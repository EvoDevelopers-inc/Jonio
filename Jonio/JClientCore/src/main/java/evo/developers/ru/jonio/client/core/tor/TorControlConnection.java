package evo.developers.ru.jonio.client.core.tor;

import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


@Slf4j
public class TorControlConnection implements AutoCloseable {

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
        log.info("Authenticated with Tor control port");
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
        
        log.info("Sent signal: {}", signal);
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


    public void authenticateWithPassword(String password) throws IOException {
        if (authenticated) return;

        String command = "AUTHENTICATE \"" + password.replace("\"", "\\\"") + "\"";
        sendCommand(command);
        String response = readResponse();

        if (!response.startsWith("250")) {
            throw new IOException("Authentication failed: " + response);
        }

        authenticated = true;
        log.info("Authenticated with Tor control port using password");
    }

    public void shutdownTor(String keyword) throws IOException {
        checkAuthenticated();
        
        if (!"SHUTDOWN".equals(keyword) && !"HALT".equals(keyword)) {
            throw new IllegalArgumentException("Invalid shutdown keyword: " + keyword);
        }
        
        sendCommand(keyword);

        log.info("Tor shutdown command sent");
    }

    private void sendCommand(String command) throws IOException {
        log.debug("Sending command: {}", command);
        writer.write(command + "\r\n");
        writer.flush();
    }

    private String readResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            log.debug("Received: {}", line);
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

    private void checkAuthenticated() throws IOException {
        if (!authenticated) {
            throw new IOException("Not authenticated with Tor control port");
        }
    }

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

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
    
    @Override
    public void close() throws IOException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                log.warn("Error closing reader", e);
            }
        }
        
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                log.warn("Error closing writer", e);
            }
        }
        
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                log.warn("Error closing socket", e);
            }
        }
        
        log.info("Tor control connection closed");
    }
}




