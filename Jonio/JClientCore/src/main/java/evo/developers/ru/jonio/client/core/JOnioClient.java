package evo.developers.ru.jonio.client.core;

import com.nimbusds.jose.jwk.OctetKeyPair;
import evo.developers.ru.jonio.client.core.base.IJwkPairFactory;
import evo.developers.ru.jonio.client.core.base.JClient;
import evo.developers.ru.jonio.client.core.cryptography.ClientHasher;
import evo.developers.ru.jonio.client.core.cryptography.JwkPairFactory;
import evo.developers.ru.jonio.client.core.dto.RequestAuthJwt;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientErrorInit;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientNotInit;
import evo.developers.ru.jonio.client.core.helpers.httpclient.TorHttpClient;
import evo.developers.ru.jonio.client.core.model.JOSession;
import evo.developers.ru.jonio.client.core.model.Settings;
import evo.developers.ru.jonio.client.core.network.p2p.connector.JOnioConnector;
import evo.developers.ru.jonio.client.core.network.sdk.JOnioApiServer;
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
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class JOnioClient extends JClient {

    public static final String VERSION_CLIENT = "ALPHA-0.9.1-l33t AIR TYPE";
    @Getter
    private JOSession session;

    @Getter
    private Settings settings;

    @Getter
    private JOnioApiServer sdkApiServer;

    @Getter
    private ClientTor torClient;

    public JOnioClient(Settings settings) {
        this.settings = settings;

        setJwkPairFactory(new JwkPairFactory());
        setClientHasher(new ClientHasher());

    }


    private void initTor(Path sessionPathFolder) throws Exception {
        log.info("Initializing Tor client...");

        torClient = new ClientTor();
        torClient.initTorBin(sessionPathFolder);
        torClient.start();
        torClient.connect();

        sdkApiServer = new JOnioApiServer(getSettings(), torClient.getTorHttpClient());

        log.info("Tor client initialized successfully");
        log.info("SOCKS proxy: :{}", torClient.getSocksPort());

        JOnioConnector.getInstance(torClient);

        String onionAddress = torClient.createOnion();
        System.out.println("onion address: " + onionAddress);
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

    public void login(String login, String password) throws Exception {
        String sSessionPathFolder = settings.getPathToSessionFolder();

        if (sSessionPathFolder.isEmpty()){
            throw new JOnioClientErrorInit("No session folder provided");
        }

        Path sessionPathFolder = Paths.get(sSessionPathFolder);

        initSession(sessionPathFolder);
        initTor(sessionPathFolder);

        String clientHash = getClientHasher().computeClientHash(login, password);
        OctetKeyPair octetKeyPair = getJwkPairFactory().generateKeyPair();

        log.info("Client hash: {}", clientHash);

        log.info("Client Public key: {}", getJwkPairFactory().getPublicJwk(octetKeyPair));
        log.info("Client Private key: {}", getJwkPairFactory().getPrivateJwk(octetKeyPair));

        sdkApiServer.auth(RequestAuthJwt.builder()
                        .hashClient(clientHash)
                        .pubKeyBase64(Base64.getEncoder().encodeToString(getJwkPairFactory().getPublicJwk(octetKeyPair).getBytes()))
                        .build()
                );
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

    @Override
    public void setJwkPairFactory(IJwkPairFactory jwkPairFactory) {
        jwkPairFactory = jwkPairFactory;
    }

    @Override
    public IJwkPairFactory getJwkPairFactory() {
        return null;
    }
}
