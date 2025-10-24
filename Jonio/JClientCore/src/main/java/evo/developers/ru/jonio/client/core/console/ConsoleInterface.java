package evo.developers.ru.jonio.client.core.console;

import evo.developers.ru.jonio.client.core.helpers.httpclient.TorHttpClient;
import evo.developers.ru.jonio.client.core.network.p2p.connector.JOnioConnector;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static evo.developers.ru.jonio.client.core.JOnioClient.VERSION_CLIENT;


@Slf4j
public class ConsoleInterface implements Runnable {
    
    private final JOnioConnector connector;
    private final TorHttpClient httpClient;
    private final String myOnionAddress;
    private final BufferedReader reader;
    private boolean running = true;
    

    private String activeConnection = null;
    private Map<String, String> connections = new HashMap<>();
    
    public ConsoleInterface(JOnioConnector connector, TorHttpClient httpClient, String myOnionAddress) {
        this.connector = connector;
        this.httpClient = httpClient;
        this.myOnionAddress = myOnionAddress;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }
    
    @Override
    public void run() {
        printWelcome();
        
        while (running) {
            try {
                System.out.print("> ");
                String input = reader.readLine();
                
                if (input == null || input.trim().isEmpty()) {
                    continue;
                }
                
                processCommand(input.trim());
            } catch (Exception e) {
                log.error("Error processing command", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    
    private void printWelcome() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.printf("║         Jonio P2P Messenger - Tor Anonymous Messenger %s        ║%n", VERSION_CLIENT);
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("\nYour onion address: " + myOnionAddress);
        System.out.println("\nAvailable commands:");
        System.out.println("  connect <onion_address> [name] - Connect to another peer");
        System.out.println("  send <message>                 - Send message to active connection");
        System.out.println("  select <number>                - Select active connection");
        System.out.println("  list                           - List all connections");
        System.out.println("  help                           - Show this help");
        System.out.println("  exit                           - Exit application");
        System.out.println();
    }
    
    private void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (command) {
            case "connect":
                handleConnect(args);
                break;
            case "send":
                handleSend(args);
                break;
            case "select":
                handleSelect(args);
                break;
            case "list":
                handleList();
                break;
            case "help":
                printWelcome();
                break;
            case "exit":
            case "quit":
                handleExit();
                break;
            default:
                if (activeConnection != null) {
                    handleSend(input);
                } else {
                    System.out.println("Unknown command: " + command + ". Type 'help' for help.");
                }
        }
    }
    
    private void handleConnect(String args) {
        if (args.isEmpty()) {
            System.out.println("Usage: connect <onion_address> [name]");
            return;
        }
        
        String[] parts = args.split("\\s+", 2);
        String onionAddress = parts[0];
        String name = parts.length > 1 ? parts[1] : "Anonymous";

        if (!onionAddress.endsWith(".onion")) {
            System.out.println("Error: Invalid onion address. Must end with .onion");
            return;
        }
        
        System.out.println("Connecting to " + onionAddress + "...");
        
        try {
            String response = httpClient.sendConnectRequest(onionAddress, myOnionAddress, name);
            System.out.println("✓ Connected successfully!");

            connections.put(onionAddress, name);
            activeConnection = onionAddress;
            
            System.out.println("Active connection set to: " + onionAddress);
        } catch (Exception e) {
            log.error("Connection failed", e);
            System.out.println("✗ Connection failed: " + e.getMessage());
        }
    }
    
    private void handleSend(String message) {
        if (message.isEmpty()) {
            System.out.println("Usage: send <message>");
            return;
        }
        
        if (activeConnection == null) {
            System.out.println("Error: No active connection. Use 'connect' first.");
            return;
        }
        
        try {
            httpClient.sendMessage(activeConnection, myOnionAddress, message);
            System.out.println("[You -> " + connections.get(activeConnection) + "]: " + message);
        } catch (Exception e) {
            log.error("Failed to send message", e);
            System.out.println("✗ Failed to send message: " + e.getMessage());
        }
    }
    
    private void handleSelect(String args) {
        if (args.isEmpty()) {
            System.out.println("Usage: select <number>");
            handleList();
            return;
        }
        
        try {
            int index = Integer.parseInt(args) - 1;
            String[] addresses = connections.keySet().toArray(new String[0]);
            
            if (index < 0 || index >= addresses.length) {
                System.out.println("Error: Invalid connection number");
                return;
            }
            
            activeConnection = addresses[index];
            System.out.println("Active connection set to: " + connections.get(activeConnection) + " (" + activeConnection + ")");
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format");
        }
    }
    
    private void handleList() {
        if (connections.isEmpty()) {
            System.out.println("No connections yet.");
            return;
        }
        
        System.out.println("\nConnections:");
        int i = 1;
        for (Map.Entry<String, String> entry : connections.entrySet()) {
            String marker = entry.getKey().equals(activeConnection) ? "* " : "  ";
            System.out.println(marker + i + ". " + entry.getValue() + " (" + entry.getKey() + ")");
            i++;
        }
        System.out.println();
    }
    
    private void handleExit() {
        System.out.println("Shutting down...");
        running = false;
        System.exit(0);
    }
    
    public void stop() {
        running = false;
    }
}

