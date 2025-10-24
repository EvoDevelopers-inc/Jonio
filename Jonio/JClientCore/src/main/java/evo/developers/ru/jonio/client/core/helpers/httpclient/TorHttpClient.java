package evo.developers.ru.jonio.client.core.helpers.httpclient;

import com.google.gson.Gson;
import evo.developers.ru.jonio.client.core.dto.ConnectRequest;
import evo.developers.ru.jonio.client.core.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * HTTP клиент для работы через Tor SOCKS прокси
 */
public class TorHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(TorHttpClient.class);
    
    private final String socksHost;
    private final int socksPort;
    private final Gson gson;
    private final Proxy proxy;
    
    public TorHttpClient(String socksHost, int socksPort) {
        this.socksHost = socksHost;
        this.socksPort = socksPort;
        this.gson = new Gson();
        this.proxy = new Proxy(Proxy.Type.SOCKS, 
            new InetSocketAddress(socksHost, socksPort));
    }
    
    /**
     * Выполняет GET запрос через Tor SOCKS прокси
     */
    public String get(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(60000); // 60 секунд для .onion
        connection.setReadTimeout(60000);
        
        return readResponse(connection);
    }
    
    /**
     * Выполняет POST запрос с JSON телом через Tor SOCKS прокси
     */
    public String post(String urlString, Object body) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(60000); // 60 секунд для .onion
        connection.setReadTimeout(60000);
        connection.setDoOutput(true);
        
        // Отправляем JSON тело
        String jsonBody = gson.toJson(body);
        logger.info("Sending POST to {}: {}", urlString, jsonBody);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        return readResponse(connection);
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        logger.debug("Response code: {}", responseCode);
        
        InputStream inputStream = responseCode < 400 
            ? connection.getInputStream() 
            : connection.getErrorStream();
            
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }
        
        return response.toString().trim();
    }

    public String sendConnectRequest(String targetOnionAddress, String myOnionAddress, String myName) throws IOException {
        String url = String.format("http://%s/connect", targetOnionAddress);
        
        ConnectRequest connectRequest = new ConnectRequest();
        connectRequest.setSenderName(myName);
        connectRequest.setSenderOnionAddress(myOnionAddress);
        logger.info("Connecting to: {}", url);
        return post(url, connectRequest);
    }
    

    public String sendMessage(String targetOnionAddress, String myOnionAddress, String messageText) throws IOException {
        String url = String.format("http://%s/message", targetOnionAddress);
        
        Message messageObj = new Message();
        messageObj.setText(messageText);
        messageObj.setTimestamp(System.currentTimeMillis());
        messageObj.setSenderOnionAddress(targetOnionAddress);

        logger.info("Sending message to: {}", url);
        return post(url, messageObj);
    }
    

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




