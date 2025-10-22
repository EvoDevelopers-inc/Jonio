import evo.developers.ru.jonio.client.core.tor.ClientTor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            // Пример использования ClientTor
            logger.info("Starting Tor client example...");
            
            // Создаем экземпляр ClientTor
            ClientTor torClient = new ClientTor();
            
            // Указываем папку сессии (можно использовать любую)
            Path sessionFolder = Paths.get(System.getProperty("user.home"), ".jonio", "session");
            
            // Инициализируем Tor (распаковываем бинарник по архитектуре)
            logger.info("Initializing Tor binary...");
            torClient.initTorBin(sessionFolder);
            
            // Запускаем Tor
            logger.info("Starting Tor...");
            torClient.start();
            
            // Подключаемся к контроллеру Tor
            logger.info("Connecting to Tor control port...");
            torClient.connect();
            
            logger.info("Tor is running!");
            logger.info("SOCKS proxy available at: 127.0.0.1:{}", torClient.getSocksPort());
            logger.info("Control port: {}", torClient.getControlPort());
            
            // Пример получения новой идентичности
            Thread.sleep(3000);
            logger.info("Requesting new identity...");
            torClient.newIdentity();
            
            // Ждем немного
            Thread.sleep(5000);
            
            // Останавливаем Tor
            logger.info("Stopping Tor...");
            torClient.stop();
            
            logger.info("Example completed successfully!");
            
        } catch (Exception e) {
            logger.error("Error running Tor client example", e);
            System.exit(1);
        }
    }
}
