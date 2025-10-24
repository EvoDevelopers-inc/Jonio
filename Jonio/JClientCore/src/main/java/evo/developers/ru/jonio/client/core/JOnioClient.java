package evo.developers.ru.jonio.client.core;

import evo.developers.ru.jonio.client.core.base.JClient;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientErrorInit;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientNotInit;
import evo.developers.ru.jonio.client.core.helpers.httpclient.TorHttpClient;
import evo.developers.ru.jonio.client.core.model.JOSession;
import evo.developers.ru.jonio.client.core.model.Settings;
import evo.developers.ru.jonio.client.core.network.p2p.connector.JOnioConnector;
import evo.developers.ru.jonio.client.core.tor.ClientTor;
import evo.developers.ru.jonio.client.core.tor.TorControlConnection;
import io.javalin.Javalin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class JOnioClient extends JClient {

    public static final String VERSION_CLIENT = "ALPHA-0.9.1-l33t AIR TYPE";
    @Getter
    private JOSession session;
    private Settings settings;
    
    @Getter
    private ClientTor torClient;

    public JOnioClient(Settings settings) {
        this.settings = settings;
    }


    public void initialize() throws Exception {
        String sSessionPathFolder  = settings.getPathToSessionFolder();

        if (sSessionPathFolder.isEmpty()){
            throw new JOnioClientErrorInit("No session folder provided");
        }

        Path sessionPathFolder = Paths.get(sSessionPathFolder);

        initSession(sessionPathFolder);

        initTor(sessionPathFolder);
    }


    private void initTor(Path sessionPathFolder) throws Exception {
        log.info("Initializing Tor client...");

        torClient = new ClientTor();
        torClient.initTorBin(sessionPathFolder);
        torClient.start();
        torClient.connect();

        log.info("Tor client initialized successfully");
        log.info("SOCKS proxy: :{}", torClient.getSocksPort());

        JOnioConnector.getInstance(torClient);

        String onionAdress = torClient.createOnion();
        System.out.println("onion adress: " + onionAdress);
    }



    public void shutdown() throws Exception {

        if (torClient != null) {
            log.info("Shutting down Tor...");
            torClient.stop();
        }
    }

    public String getOnionAddress() {
        return torClient.getOnionAddress();
    }

    private void login() throws Exception {
        String sSessionPathFolder  = settings.getPathToSessionFolder();

        if (sSessionPathFolder.isEmpty()){
            throw new JOnioClientErrorInit("No session folder provided");
        }

        Path sessionPathFolder = Paths.get(sSessionPathFolder);

        initSession(sessionPathFolder);
    }

    private void initSession(Path sessionPathFolder) throws Exception{
        File sessionFile = new File(sessionPathFolder.toFile(), "jo-session.json");

        if (!sessionFile.exists()){
             session = new JOSession();

             return;
        }

        if (!sessionFile.canWrite()){
            throw new JOnioClientErrorInit("Cannot write to session file");
        }

        try (BufferedReader br = new BufferedReader(new FileReader("example.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }

    }
}
