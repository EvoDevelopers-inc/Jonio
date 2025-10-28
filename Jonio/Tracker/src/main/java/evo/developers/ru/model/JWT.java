package evo.developers.ru.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JWT {
    private String token;
    private String refreshToken;
}
