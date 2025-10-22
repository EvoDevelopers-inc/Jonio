package evo.developers.ru.dto;

import lombok.Data;

@Data
public class ResponseAuthJwt {
    private final String tokenRefresh;
    private final String token;
}
