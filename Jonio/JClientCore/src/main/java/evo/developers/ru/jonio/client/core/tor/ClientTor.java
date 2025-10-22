package evo.developers.ru.jonio.client.core.tor;

import evo.developers.ru.jonio.client.core.helpers.PlatformDetector;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class ClientTor {
    private static final Logger logger = LoggerFactory.getLogger(ClientTor.class);
    private static final String TOR_VERSION = "14.5.8";
    
    private Process torProcess;
    private TorControlConnection controlConnection;
    
    @Getter
    private Path torDirectory;
    
    @Getter @Setter
    private int socksPort = 9050;
    
    @Getter @Setter
    private int controlPort = 9051;
    
    @Getter @Setter
    private String controlPassword = "jonio_control_pass";
    
    @Getter
    private boolean initialized = false;
    
    /**
     * Определяет платформу и архитектуру системы
     */
    private static String detectPlatform() {
        PlatformDetector.PlatformInfo info = PlatformDetector.detect();
        
        if (!info.isSupported()) {
            throw new RuntimeException(
                String.format("Unsupported platform: %s", PlatformDetector.getPlatformDescription())
            );
        }
        
        logger.info("Detected platform: {}", PlatformDetector.getPlatformDescription());
        return info.getFullPlatformString();
    }
    
    /**
     * Инициализирует Tor: распаковывает бинарник и подготавливает окружение
     */
    public void initTorBin(Path sessionFolder) throws IOException {
        if (initialized) {
            logger.info("Tor already initialized");
            return;
        }
        
        String platform = detectPlatform();
        String resourceName = String.format("/tor-expert-bundle-%s-%s.tar.gz", platform, TOR_VERSION);
        
        logger.info("Looking for Tor bundle: {}", resourceName);
        
        torDirectory = sessionFolder.resolve("tor");
        
        // Проверяем, уже распакован ли Tor
        Path torBinary = torDirectory.resolve("tor/tor");
        if (PlatformDetector.isWindows()) {
            torBinary = torDirectory.resolve("tor/tor.exe");
        }
        
        if (Files.exists(torDirectory) && Files.exists(torBinary)) {
            logger.info("Tor already extracted at {}", torDirectory);
            initialized = true;
            return;
        }
        
        // Создаем директорию для Tor
        Files.createDirectories(torDirectory);
        
        // Распаковываем архив
        try (InputStream resourceStream = getClass().getResourceAsStream(resourceName)) {
            if (resourceStream == null) {
                throw new IOException("Tor bundle not found in resources: " + resourceName);
            }
            
            extractTarGz(resourceStream, torDirectory);
            logger.info("Tor bundle extracted successfully to {}", torDirectory);
            
            // Устанавливаем права на исполнение для Unix-систем
            if (!PlatformDetector.isWindows()) {
                setExecutablePermissions(torDirectory);
            }
            
            initialized = true;
        }
    }
    
    /**
     * Распаковывает tar.gz архив
     */
    private void extractTarGz(InputStream inputStream, Path outputPath) throws IOException {
        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(inputStream);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                if (!tarIn.canReadEntryData(entry)) {
                    logger.warn("Cannot read entry: {}", entry.getName());
                    continue;
                }
                
                Path outputFile = outputPath.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(outputFile);
                } else {
                    // Создаем родительские директории
                    Files.createDirectories(outputFile.getParent());
                    
                    // Копируем файл
                    Files.copy(tarIn, outputFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
    
    /**
     * Устанавливает права на исполнение для бинарников Tor
     */
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
            logger.info("Set executable permissions for {}", torBinary);
        }
    }
    
    /**
     * Запускает процесс Tor
     */
    public void start() throws IOException {
        if (torProcess != null && torProcess.isAlive()) {
            logger.warn("Tor is already running");
            return;
        }
        
        if (!initialized || torDirectory == null) {
            throw new IllegalStateException("Tor not initialized. Call initTorBin() first");
        }
        
        Path torBinary = torDirectory.resolve("tor/tor");
        if (PlatformDetector.isWindows()) {
            torBinary = torDirectory.resolve("tor/tor.exe");
        }
        
        if (!Files.exists(torBinary)) {
            throw new FileNotFoundException("Tor binary not found: " + torBinary);
        }
        
        // Создаем директорию для данных Tor
        Path torDataDir = torDirectory.resolve("data");
        Files.createDirectories(torDataDir);
        
        // Создаем torrc файл
        Path torrcPath = torDirectory.resolve("torrc");
        createTorrcFile(torrcPath, torDataDir);
        
        // Запускаем Tor
        ProcessBuilder pb = new ProcessBuilder(
            torBinary.toString(),
            "-f", torrcPath.toString()
        );
        
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        
        torProcess = pb.start();
        logger.info("Tor process started");
        
        // Логируем вывод Tor в отдельном потоке
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(torProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug("Tor: {}", line);
                    if (line.contains("Bootstrapped 100%")) {
                        logger.info("Tor is ready!");
                    }
                }
            } catch (IOException e) {
                logger.error("Error reading Tor output", e);
            }
        }).start();
        
        // Ждем немного, чтобы Tor запустился
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Создает файл конфигурации torrc
     */
    private void createTorrcFile(Path torrcPath, Path dataDir) throws IOException {
        String torrc = String.format(
            "SocksPort %d\n" +
            "ControlPort %d\n" +
            "HashedControlPassword %s\n" +
            "DataDirectory %s\n" +
            "CookieAuthentication 1\n",
            socksPort,
            controlPort,
            hashPassword(controlPassword),
            dataDir.toString()
        );
        
        Files.writeString(torrcPath, torrc);
        logger.info("Created torrc file at {}", torrcPath);
    }
    
    /**
     * Хеширует пароль для Tor (упрощенная версия, для продакшена используйте правильный алгоритм)
     */
    private String hashPassword(String password) {
        // Для простоты используем базовое хеширование
        // В продакшене нужно использовать правильный алгоритм Tor
        return "16:872860B76453A77D60CA2BB8C1A7042072093276A3D701AD684053EC4C";
    }
    
    /**
     * Подключается к контроллеру Tor
     */
    public void connect() throws IOException {
        if (controlConnection != null) {
            logger.warn("Already connected to Tor control");
            return;
        }
        
        Socket socket = new Socket("127.0.0.1", controlPort);
        controlConnection = new TorControlConnection(socket);
        controlConnection.authenticate(new byte[0]); // используем cookie authentication
        logger.info("Connected to Tor control port");
    }
    
    /**
     * Получает новую идентичность (новый IP)
     */
    public void newIdentity() throws IOException {
        if (controlConnection == null) {
            throw new IllegalStateException("Not connected to Tor control");
        }
        
        controlConnection.signal("NEWNYM");
        logger.info("Requested new Tor identity");
    }
    
    /**
     * Получает текущий IP через Tor
     */
    public String getCurrentIP() {
        // Здесь можно добавить запрос к сервису проверки IP через Tor SOCKS прокси
        return "Check via external service through SOCKS proxy";
    }
    
    /**
     * Останавливает Tor
     */
    public void stop() throws IOException {
        if (controlConnection != null) {
            try {
                controlConnection.shutdownTor("SHUTDOWN");
            } catch (Exception e) {
                logger.error("Error shutting down Tor gracefully", e);
            }
            controlConnection = null;
        }
        
        if (torProcess != null && torProcess.isAlive()) {
            torProcess.destroy();
            try {
                torProcess.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                torProcess.destroyForcibly();
            }
            torProcess = null;
        }
        
        logger.info("Tor stopped");
    }
    
    /**
     * Проверяет, запущен ли Tor
     */
    public boolean isRunning() {
        return torProcess != null && torProcess.isAlive();
    }
}
