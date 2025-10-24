package evo.developers.ru.jonio.client.core;

import evo.developers.ru.jonio.client.core.base.JClient;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientErrorInit;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientNotInit;
import evo.developers.ru.jonio.client.core.model.JOSession;
import evo.developers.ru.jonio.client.core.model.Settings;
import evo.developers.ru.jonio.client.core.tor.ClientTor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class JOnioClient extends JClient {

    @Getter
    private JOSession session;
    private Settings settings;
    
    @Getter
    private ClientTor torClient;


    public JOnioClient(Settings settings) {
        this.settings = settings;
    }


    /**
     * Инициализирует клиент с поддержкой Tor
     */
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
        log.info("SOCKS proxy: 127.0.0.1:{}", torClient.getSocksPort());
    }
    
    /**
     * Останавливает клиент и Tor
     */
    public void shutdown() throws Exception {
        if (torClient != null) {
            log.info("Shutting down Tor...");
            torClient.stop();
        }
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
