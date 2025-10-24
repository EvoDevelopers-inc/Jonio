package evo.developers.ru.jonio.client.core.network.p2p.connector.http;

import com.google.gson.Gson;
import evo.developers.ru.jonio.client.core.dto.ConnectRequest;
import evo.developers.ru.jonio.client.core.dto.Message;
import evo.developers.ru.jonio.client.core.dto.MessageResponse;
import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
abstract public class JOnioAPI {

    protected final Map<String, String> connectedPeers = new ConcurrentHashMap<>();
    protected final Gson gson = new Gson();

    protected void index(Context ctx) {
        ctx.result("Jonio P2P Messenger - Active connections: " + connectedPeers.size());
    }
    

    protected void handleConnect(Context ctx) {
        try {
            String body = ctx.body();
            log.info("Received connect request body: {}", body);
            
            if (body == null || body.trim().isEmpty()) {
                log.error("Empty request body");
                ctx.status(400).json(MessageResponse.error("Empty request body"));
                return;
            }
            
            ConnectRequest request = gson.fromJson(body, ConnectRequest.class);
            
            if (request == null) {
                log.error("Failed to parse ConnectRequest from JSON: {}", body);
                ctx.status(400).json(MessageResponse.error("Failed to parse request"));
                return;
            }
            
            if (request.getSenderOnionAddress() == null || request.getSenderOnionAddress().isEmpty()) {
                log.error("Missing sender onion address");
                ctx.status(400).json(MessageResponse.error("Missing sender onion address"));
                return;
            }
            
            log.info("Connection request from: {} ({})", 
                request.getSenderOnionAddress(), 
                request.getSenderName() != null ? request.getSenderName() : "Anonymous");

            String peerName = request.getSenderName() != null ? request.getSenderName() : "Anonymous";
            connectedPeers.put(request.getSenderOnionAddress(), peerName);
            
            System.out.println("\n[NEW CONNECTION] " + peerName + " (" + request.getSenderOnionAddress() + ")");
            System.out.print("> ");
            
            ctx.json(MessageResponse.success());
        } catch (Exception e) {
            log.error("Error handling connect request", e);
            ctx.status(400).json(MessageResponse.error("Invalid request: " + e.getMessage()));
        }
    }
    

    protected void handleMessage(Context ctx) {
        try {
            String body = ctx.body();
            log.info("Received message body: {}", body);
            
            if (body == null || body.trim().isEmpty()) {
                log.error("Empty message body");
                ctx.status(400).json(MessageResponse.error("Empty message body"));
                return;
            }
            
            Message message = gson.fromJson(body, Message.class);
            
            if (message == null) {
                log.error("Failed to parse Message from JSON: {}", body);
                ctx.status(400).json(MessageResponse.error("Failed to parse message"));
                return;
            }
            
            if (message.getSenderOnionAddress() == null || message.getText() == null) {
                log.error("Invalid message format");
                ctx.status(400).json(MessageResponse.error("Invalid message format"));
                return;
            }
            
            log.info("Message from {}: {}", message.getSenderOnionAddress(), message.getText());

            String senderName = connectedPeers.get(message.getSenderOnionAddress());
            if (senderName == null) {
                senderName = "Unknown";
            }

            System.out.println("\n[" + senderName + "]: " + message.getText());
            System.out.print("> ");
            
            ctx.json(MessageResponse.success());
        } catch (Exception e) {
            log.error("Error handling message", e);
            ctx.status(400).json(MessageResponse.error("Invalid message: " + e.getMessage()));
        }
    }

    protected void listPeers(Context ctx) {
        ctx.json(connectedPeers);
    }
}
