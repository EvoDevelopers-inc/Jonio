import evo.developers.ru.jonio.client.core.JOnioClient;
import evo.developers.ru.jonio.client.core.console.ConsoleInterface;
import evo.developers.ru.jonio.client.core.helpers.httpclient.TorHttpClient;
import evo.developers.ru.jonio.client.core.model.Settings;
import evo.developers.ru.jonio.client.core.network.p2p.connector.JOnioConnector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {

        Settings settings = new Settings("./session", "localhost:8080");
        JOnioClient.init(settings);
        JOnioClient client = JOnioClient.getInstance();
        client.login("login", "password");

        JOnioConnector connector = JOnioConnector.getInstance(client.getTorClient());
        String myOnionAddress = client.getTorClient().getOnionAddress();
        
        TorHttpClient httpClient = new TorHttpClient(
            client.getTorClient().getSOCKS_PROXY_HOST(),
            client.getTorClient().getSocksPort()
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Shutting down...");
                connector.stop();
                client.shutdown();
            } catch (Exception e) {
                log.error("Error during shutdown", e);
            }
        }));

        log.info("Waiting for hidden service to be published in Tor network...");
        Thread.sleep(15000);

        log.info("Starting console interface...");
        ConsoleInterface console = new ConsoleInterface(connector, httpClient, myOnionAddress);
        Thread consoleThread = new Thread(console);
        consoleThread.setDaemon(false);
        consoleThread.start();


        consoleThread.join();
    }
}
