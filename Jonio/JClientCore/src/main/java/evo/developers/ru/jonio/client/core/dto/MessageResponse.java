package evo.developers.ru.jonio.client.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ на сообщение
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private boolean success;
    private String message;
    
    public static MessageResponse success() {
        return new MessageResponse(true, "Message received");
    }
    
    public static MessageResponse error(String message) {
        return new MessageResponse(false, message);
    }
}

