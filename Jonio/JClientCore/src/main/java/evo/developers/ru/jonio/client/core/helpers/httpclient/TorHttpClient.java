package evo.developers.ru.jonio.client.core.helpers.httpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * HTTP клиент для работы через Tor SOCKS прокси
 */
public class TorHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(TorHttpClient.class);
    
    private final String socksHost;
    private final int socksPort;
    
    public TorHttpClient(String socksHost, int socksPort) {
        this.socksHost = socksHost;
        this.socksPort = socksPort;
    }
    
    /**
     * Выполняет GET запрос через Tor SOCKS прокси
     */
    public String get(String urlString) throws IOException {
        // Создаем SOCKS прокси
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, 
            new InetSocketAddress(socksHost, socksPort));
        
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection(proxy);
        connection.setConnectTimeout(30000); // 30 секунд
        connection.setReadTimeout(30000);
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }
        
        return response.toString();
    }
    
    /**
     * Получает текущий IP адрес через сервис проверки
     */
    public String checkIP() {
        try {
            String response = get("https://check.torproject.org/api/ip");
            logger.info("IP check response: {}", response);
            return response;
        } catch (IOException e) {
            logger.error("Failed to check IP", e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Проверяет, работает ли Tor
     */
    public boolean isTorWorking() {
        try {
            String response = get("https://check.torproject.org/");
            return response.contains("Congratulations");
        } catch (IOException e) {
            logger.error("Tor connection check failed", e);
            return false;
        }
    }
}




