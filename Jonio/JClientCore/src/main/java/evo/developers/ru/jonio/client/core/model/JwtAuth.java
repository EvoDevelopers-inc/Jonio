package evo.developers.ru.jonio.client.core.model;

import lombok.Data;

@Data
public class JwtAuth {
    private final String token;
    private final String refreshToken;
}
