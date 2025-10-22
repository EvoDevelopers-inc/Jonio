package evo.developers.ru.jonio.client.core.tor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Упрощенная реализация Tor Control Connection
 * Основана на Tor Control Protocol specification
 */
public class TorControlConnection implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TorControlConnection.class);
    
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private boolean authenticated = false;
    
    public TorControlConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1)
        );
        this.writer = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1)
        );
    }
    
    /**
     * Аутентифицируется с Tor через cookie authentication
     */
    public void authenticate(byte[] cookie) throws IOException {
        if (authenticated) {
            return;
        }
        
        // Используем NULL authentication (для простоты)
        sendCommand("AUTHENTICATE");
        String response = readResponse();
        
        if (!response.startsWith("250")) {
            throw new IOException("Authentication failed: " + response);
        }
        
        authenticated = true;
        logger.info("Authenticated with Tor control port");
    }
    
    /**
     * Отправляет сигнал Tor
     */
    public void signal(String signal) throws IOException {
        checkAuthenticated();
        
        sendCommand("SIGNAL " + signal);
        String response = readResponse();
        
        if (!response.startsWith("250")) {
            throw new IOException("Signal failed: " + response);
        }
        
        logger.info("Sent signal: {}", signal);
    }
    
    /**
     * Получает информацию от Tor
     */
    public String getInfo(String key) throws IOException {
        checkAuthenticated();
        
        sendCommand("GETINFO " + key);
        String response = readResponse();
        
        if (!response.startsWith("250")) {
            throw new IOException("GETINFO failed: " + response);
        }
        
        // Парсим ответ (формат: 250-key=value или 250 OK)
        if (response.contains("=")) {
            String[] parts = response.split("=", 2);
            if (parts.length == 2) {
                return parts[1].trim();
            }
        }
        
        return response;
    }
    
    /**
     * Устанавливает конфигурацию Tor
     */
    public void setConf(String key, String value) throws IOException {
        checkAuthenticated();
        
        sendCommand("SETCONF " + key + "=" + quoteString(value));
        String response = readResponse();
        
        if (!response.startsWith("250")) {
            throw new IOException("SETCONF failed: " + response);
        }
        
        logger.info("Set configuration: {}={}", key, value);
    }
    
    /**
     * Останавливает Tor
     */
    public void shutdownTor(String keyword) throws IOException {
        checkAuthenticated();
        
        if (!"SHUTDOWN".equals(keyword) && !"HALT".equals(keyword)) {
            throw new IllegalArgumentException("Invalid shutdown keyword: " + keyword);
        }
        
        sendCommand(keyword);
        // Не читаем ответ, так как Tor может закрыть соединение
        logger.info("Tor shutdown command sent");
    }
    
    /**
     * Отправляет команду Tor
     */
    private void sendCommand(String command) throws IOException {
        logger.debug("Sending command: {}", command);
        writer.write(command + "\r\n");
        writer.flush();
    }
    
    /**
     * Читает ответ от Tor
     */
    private String readResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            logger.debug("Received: {}", line);
            response.append(line);
            
            // Ответы Tor заканчиваются линией, начинающейся с кода без дефиса
            // 250-... продолжение
            // 250 ... конец
            if (line.length() >= 4 && line.charAt(3) == ' ') {
                break;
            }
            
            response.append("\n");
        }
        
        return response.toString();
    }
    
    /**
     * Проверяет, аутентифицированы ли мы
     */
    private void checkAuthenticated() throws IOException {
        if (!authenticated) {
            throw new IOException("Not authenticated with Tor control port");
        }
    }
    
    /**
     * Экранирует строку для использования в командах Tor
     */
    private String quoteString(String s) {
        if (s == null) {
            return "\"\"";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        
        for (char c : s.toCharArray()) {
            if (c == '\\' || c == '"') {
                sb.append('\\');
            }
            sb.append(c);
        }
        
        sb.append('"');
        return sb.toString();
    }
    
    /**
     * Проверяет, открыто ли соединение
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
    
    @Override
    public void close() throws IOException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.warn("Error closing reader", e);
            }
        }
        
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.warn("Error closing writer", e);
            }
        }
        
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn("Error closing socket", e);
            }
        }
        
        logger.info("Tor control connection closed");
    }
}

