package evo.developers.ru.jonio.client.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JOSession {
    private String refreshToken;
    private String token;
    private String username;
    private byte[] imgAvatar;
}
