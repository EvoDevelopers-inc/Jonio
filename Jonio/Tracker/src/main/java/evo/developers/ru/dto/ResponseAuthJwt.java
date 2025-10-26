package evo.developers.ru.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseAuthJwt {
    private final String tokenRefresh;
    private final String token;
}
