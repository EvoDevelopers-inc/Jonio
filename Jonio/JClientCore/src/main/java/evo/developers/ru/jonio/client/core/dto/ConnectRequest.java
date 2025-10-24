package evo.developers.ru.jonio.client.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectRequest {

    private String senderOnionAddress;
    

    private String senderName;
}

