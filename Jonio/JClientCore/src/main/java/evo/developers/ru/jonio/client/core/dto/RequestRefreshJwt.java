package evo.developers.ru.jonio.client.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RequestRefreshJwt {

    private final String refreshToken;
    private final String token;
}
