import evo.developers.ru.jonio.client.core.JOnioClient;
import evo.developers.ru.jonio.client.core.model.Settings;
import evo.developers.ru.jonio.client.core.tor.ClientTor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {

        Settings settings = new Settings("./session");
        JOnioClient.init(settings);
        JOnioClient client = JOnioClient.getInstance();
        client.initialize();

        // Добавляем shutdown hook для корректного завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Shutting down...");
                client.shutdown();
            } catch (Exception e) {
                log.error("Error during shutdown", e);
            }
        }));

        log.info("==============================================");
        log.info("JOnio Client is running!");
        log.info("Onion address: {}", client.getOnionAddress());
        log.info("Press Ctrl+C to stop");
        log.info("==============================================");

        // Держим приложение запущенным
        Thread.currentThread().join();
    }
}
