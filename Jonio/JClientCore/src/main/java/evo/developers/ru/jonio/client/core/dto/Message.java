package evo.developers.ru.jonio.client.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сообщение между onion клиентами
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String senderOnionAddress;
    

    private String text;
    

    private long timestamp;
    
    public Message(String senderOnionAddress, String text) {
        this.senderOnionAddress = senderOnionAddress;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }
}

