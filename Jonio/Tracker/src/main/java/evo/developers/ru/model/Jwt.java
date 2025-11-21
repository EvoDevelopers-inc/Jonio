package evo.developers.ru.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Jwt {
    private String token;
    private String refreshToken;
}
