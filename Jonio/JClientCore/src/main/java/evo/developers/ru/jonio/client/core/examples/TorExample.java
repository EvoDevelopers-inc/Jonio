package evo.developers.ru.jonio.client.core.examples;

import evo.developers.ru.jonio.client.core.helpers.httpclient.TorHttpClient;
import evo.developers.ru.jonio.client.core.tor.ClientTor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Расширенный пример использования Tor с различными сценариями
 */
public class TorExample {
    private static final Logger logger = LoggerFactory.getLogger(TorExample.class);
    
    public static void main(String[] args) {
        try {
            // Пример 1: Базовая инициализация и запуск
            basicExample();
            
            // Пример 2: Работа с HTTP через Tor
            // httpExample();
            
            // Пример 3: Смена идентичности
            // changeIdentityExample();
            
        } catch (Exception e) {
            logger.error("Error in example", e);
        }
    }
    
    /**
     * Базовый пример: инициализация и запуск Tor
     */
    private static void basicExample() throws Exception {
        logger.info("=== Базовый пример ===");
        
        ClientTor torClient = new ClientTor();
        Path sessionFolder = Paths.get(System.getProperty("user.home"), ".jonio", "session");
        
        try {
            // Инициализируем
            logger.info("Инициализация Tor...");
            torClient.initTorBin(sessionFolder);
            
            // Запускаем
            logger.info("Запуск Tor...");
            torClient.start();
            
            // Подключаемся к контроллеру
            logger.info("Подключение к control port...");
            torClient.connect();
            
            // Информация о подключении
            logger.info("Tor запущен!");
            logger.info("SOCKS прокси: 127.0.0.1:{}", torClient.getSocksPort());
            logger.info("Control порт: {}", torClient.getControlPort());
            logger.info("Директория Tor: {}", torClient.getTorDirectory());
            
            // Ждем немного
            Thread.sleep(5000);
            
        } finally {
            // Всегда останавливаем Tor
            logger.info("Остановка Tor...");
            torClient.stop();
            logger.info("Tor остановлен");
        }
    }
    
    /**
     * Пример работы с HTTP через Tor
     */
    private static void httpExample() throws Exception {
        logger.info("=== HTTP через Tor пример ===");
        
        ClientTor torClient = new ClientTor();
        Path sessionFolder = Paths.get(System.getProperty("user.home"), ".jonio", "session");
        
        try {
            // Инициализируем и запускаем Tor
            torClient.initTorBin(sessionFolder);
            torClient.start();
            torClient.connect();
            
            logger.info("Tor готов, выполняем HTTP запросы...");
            
            // Создаем HTTP клиент
            TorHttpClient httpClient = new TorHttpClient("127.0.0.1", torClient.getSocksPort());
            
            // Проверяем, работает ли Tor
            boolean isTorWorking = httpClient.isTorWorking();
            logger.info("Tor работает: {}", isTorWorking);
            
            if (isTorWorking) {
                // Получаем информацию об IP
                String ipInfo = httpClient.checkIP();
                logger.info("Информация об IP:\n{}", ipInfo);
                
                // Можно делать запросы к любым сайтам через Tor
                logger.info("Выполняем запрос к example.com...");
                String response = httpClient.get("http://example.com");
                logger.info("Получен ответ длиной {} символов", response.length());
            }
            
        } finally {
            torClient.stop();
        }
    }
    
    /**
     * Пример смены идентичности (получение нового IP)
     */
    private static void changeIdentityExample() throws Exception {
        logger.info("=== Пример смены идентичности ===");
        
        ClientTor torClient = new ClientTor();
        Path sessionFolder = Paths.get(System.getProperty("user.home"), ".jonio", "session");
        
        try {
            // Инициализируем и запускаем Tor
            torClient.initTorBin(sessionFolder);
            torClient.start();
            torClient.connect();
            
            TorHttpClient httpClient = new TorHttpClient("127.0.0.1", torClient.getSocksPort());
            
            // Проверяем первый IP
            logger.info("Проверка первого IP...");
            String ip1 = httpClient.checkIP();
            logger.info("IP 1:\n{}", ip1);
            
            // Ждем немного
            Thread.sleep(2000);
            
            // Меняем идентичность
            logger.info("Запрос новой идентичности...");
            torClient.newIdentity();
            
            // Ждем, пока Tor установит новые соединения
            logger.info("Ожидание установки новых соединений...");
            Thread.sleep(10000);
            
            // Проверяем новый IP
            logger.info("Проверка нового IP...");
            String ip2 = httpClient.checkIP();
            logger.info("IP 2:\n{}", ip2);
            
            if (!ip1.equals(ip2)) {
                logger.info("Успешно! IP изменился.");
            } else {
                logger.warn("IP не изменился. Возможно, нужно подождать дольше.");
            }
            
        } finally {
            torClient.stop();
        }
    }
    
    /**
     * Пример с обработкой ошибок
     */
    private static void errorHandlingExample() {
        logger.info("=== Пример обработки ошибок ===");
        
        ClientTor torClient = new ClientTor();
        
        try {
            // Попытка запустить Tor без инициализации
            torClient.start();
            
        } catch (IllegalStateException e) {
            logger.error("Ожидаемая ошибка: {}", e.getMessage());
            logger.info("Tor нужно сначала инициализировать через initTorBin()");
        } catch (Exception e) {
            logger.error("Неожиданная ошибка", e);
        }
        
        try {
            // Попытка получить новую идентичность без подключения
            torClient.newIdentity();
            
        } catch (IllegalStateException e) {
            logger.error("Ожидаемая ошибка: {}", e.getMessage());
            logger.info("Нужно сначала подключиться через connect()");
        } catch (Exception e) {
            logger.error("Неожиданная ошибка", e);
        }
    }
    
    /**
     * Пример правильного жизненного цикла
     */
    private static void lifecycleExample() throws Exception {
        logger.info("=== Пример жизненного цикла ===");
        
        ClientTor torClient = new ClientTor();
        Path sessionFolder = Paths.get(System.getProperty("user.home"), ".jonio", "session");
        
        try {
            // 1. Инициализация (один раз)
            if (!torClient.isRunning()) {
                logger.info("Шаг 1: Инициализация Tor...");
                torClient.initTorBin(sessionFolder);
            }
            
            // 2. Запуск
            logger.info("Шаг 2: Запуск Tor...");
            torClient.start();
            
            // 3. Подключение к контроллеру
            logger.info("Шаг 3: Подключение к control port...");
            torClient.connect();
            
            // 4. Использование
            logger.info("Шаг 4: Использование Tor...");
            logger.info("Статус: {}", torClient.isRunning() ? "Запущен" : "Остановлен");
            
            // Выполняем какие-то операции
            Thread.sleep(3000);
            
            // 5. Опционально: смена идентичности
            logger.info("Шаг 5: Смена идентичности...");
            torClient.newIdentity();
            
            Thread.sleep(5000);
            
        } finally {
            // 6. Остановка (всегда выполняется)
            logger.info("Шаг 6: Остановка Tor...");
            torClient.stop();
            logger.info("Жизненный цикл завершен");
        }
    }
}

