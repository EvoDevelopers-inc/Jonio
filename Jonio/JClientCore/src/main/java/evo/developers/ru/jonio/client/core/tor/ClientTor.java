package evo.developers.ru.jonio.client.core.tor;

import evo.developers.ru.jonio.client.core.helpers.PlatformDetector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ClientTor {

    private static final String TOR_VERSION = "14.5.8";

    private Process torProcess;
    private TorControlConnection controlConnection;

    @Getter
    private Path torDirectory;

    @Getter
    private final String SOCKS_PROXY_HOST = "127.0.0.1";
    @Getter @Setter
    private int socksPort = 9050;

    @Getter @Setter
    private int controlPort = 9051;

    @Getter @Setter
    private String controlPassword = "jonio_control_pass";

    @Getter
    private boolean initialized = false;

    @Getter
    private String onionAddress;


    private static String detectPlatform() {

        PlatformDetector.PlatformInfo info = PlatformDetector.detect();
        if (!info.isSupported()) {
            throw new RuntimeException("Unsupported platform: " + PlatformDetector.getPlatformDescription());
        }

        log.info("Detected platform: {}", PlatformDetector.getPlatformDescription());
        return info.getFullPlatformString();
    }

    // ---------------- Initialization ----------------
    public void initTorBin(Path sessionFolder) throws Exception {

        if (initialized) return;

        String platform = detectPlatform();
        String resourceName = String.format("/tor-expert-bundle-%s-%s.tar.gz", platform, TOR_VERSION);
        torDirectory = sessionFolder.resolve("tor");

        Path torBinary = torDirectory.resolve("tor/tor");
        if (PlatformDetector.isWindows()) torBinary = torDirectory.resolve("tor/tor.exe");

        if (Files.exists(torDirectory) && Files.exists(torBinary)) {
            log.info("Tor already extracted at {}", torDirectory);
            initialized = true;
            return;
        }

        Files.createDirectories(torDirectory);

        try (InputStream resourceStream = getClass().getResourceAsStream(resourceName)) {
            if (resourceStream == null) throw new IOException("Tor bundle not found: " + resourceName);
            extractTarGz(resourceStream, torDirectory);
            log.info("Tor bundle extracted successfully to {}", torDirectory);

            if (!PlatformDetector.isWindows()) {
                setExecutablePermissions(torDirectory);
                setSignCode();
            }
            initialized = true;
        }
    }

    private void extractTarGz(InputStream inputStream, Path outputPath) throws IOException {
        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(inputStream);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

            ArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                Path outputFile = outputPath.resolve(entry.getName());
                if (entry.isDirectory()) Files.createDirectories(outputFile);
                else {
                    Files.createDirectories(outputFile.getParent());
                    Files.copy(tarIn, outputFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void setExecutablePermissions(Path torDir) throws IOException {
        Path torBinary = torDir.resolve("tor/tor");
        if (Files.exists(torBinary)) {
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(torBinary, perms);
            log.info("Set executable permissions for {}", torBinary);
        }
    }

    private void setSignCode() throws IOException, InterruptedException {
        if (!PlatformDetector.isMacOS() || !PlatformDetector.isARM()) return;

        Path torBinary = torDirectory.resolve("tor/tor");
        Path libeventBinary = torDirectory.resolve("tor/libevent-2.1.7.dylib");

        ProcessBuilder signLibevent = new ProcessBuilder("codesign", "-s", "-", libeventBinary.toString());
        if (signLibevent.start().waitFor() != 0)
            throw new IOException("Failed to codesign libevent");

        ProcessBuilder signTor = new ProcessBuilder("codesign", "-s", "-", "-f", torBinary.toString());
        if (signTor.start().waitFor() != 0) throw new IOException("Failed to codesign tor");
        log.info("Successfully codesigned Tor and libevent binaries for macOS ARM");
    }

    public void start() throws IOException, InterruptedException {
        if (torProcess != null && torProcess.isAlive()) return;
        if (!initialized || torDirectory == null) throw new IllegalStateException("Tor not initialized");

        Path torBinary = torDirectory.resolve("tor/tor");
        if (PlatformDetector.isWindows()) torBinary = torDirectory.resolve("tor/tor.exe");
        if (!Files.exists(torBinary)) throw new FileNotFoundException("Tor binary not found: " + torBinary);

        Path torDataDir = torDirectory.resolve("data");
        Files.createDirectories(torDataDir);

        Path torrcPath = torDirectory.resolve("torrc");
        createTorrcFile(torrcPath, torDataDir);

        ProcessBuilder pb = new ProcessBuilder(torBinary.toString(), "-f", torrcPath.toString());
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);

        torProcess = pb.start();
        log.info("Tor process started");

        final boolean[] bootstrapped = {false};
        Thread logThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(torProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Tor: {}", line);
                    if (line.contains("Bootstrapped 100%")) bootstrapped[0] = true;
                }
            } catch (IOException e) {
                log.error("Error reading Tor output", e);
            }
        });
        logThread.start();

        int attempts = 0;
        while (!bootstrapped[0] && attempts++ < 900) {
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException(e); }
        }

        if (!bootstrapped[0]) throw new IOException("Tor did not bootstrap in time");
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void createTorrcFile(Path torrcPath, Path dataDir) throws IOException, InterruptedException {
        String hashedPassword = hashPassword(controlPassword);
        String torrc = String.format(
                "SocksPort %d\n" +
                        "ControlPort %d\n" +
                        "HashedControlPassword %s\n" +
                        "CookieAuthentication 0\n" +
                        "DataDirectory %s\n",
                socksPort, controlPort, hashedPassword, dataDir.toAbsolutePath().toString()
        );
        Files.writeString(torrcPath, torrc);
        log.info("Created torrc file at {}", torrcPath);
    }

    private String hashPassword(String password) throws IOException, InterruptedException {
        Path torBinary = torDirectory.resolve("tor/tor");
        if (PlatformDetector.isWindows()) torBinary = torDirectory.resolve("tor/tor.exe");

        ProcessBuilder pb = new ProcessBuilder(
                torBinary.toString(),
                "--hash-password",
                password
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("16:")) return line.trim();
            }
        }
        process.waitFor();
        throw new IOException("Failed to generate hashed password");
    }

    public void connect() throws IOException {
        if (controlConnection != null) return;

        Socket socket = new Socket("127.0.0.1", controlPort);
        controlConnection = new TorControlConnection(socket);


        controlConnection.authenticateWithPassword(controlPassword);
        log.info("Connected to Tor control port via password");
    }


    public void newIdentity() throws IOException {
        if (controlConnection == null) throw new IllegalStateException("Not connected");
        controlConnection.signal("NEWNYM");
        log.info("Requested new Tor identity");
    }

    public void stop() throws IOException {
        if (controlConnection != null) {
            try { controlConnection.shutdownTor("SHUTDOWN"); } catch (Exception e) { log.error("Error shutting down Tor", e); }
            controlConnection = null;
        }
        if (torProcess != null && torProcess.isAlive()) {
            torProcess.destroy();
            try { torProcess.waitFor(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); torProcess.destroyForcibly(); }
            torProcess = null;
        }
        log.info("Tor stopped");
    }

    public String createOnion() throws IOException {
        controlConnection.sendCommand("ADD_ONION NEW:ED25519-V3 Port=80,127.0.0.1:8080");
        String response = controlConnection.readResponse();

        for (String line : response.split("\n")) {
            if (line.startsWith("250-ServiceID=")) {
                onionAddress = line.substring("250-ServiceID=".length()).trim() + ".onion";
                break;
            }
        }

        return onionAddress;
    }

    public boolean isRunning() {
        return torProcess != null && torProcess.isAlive();
    }
}
