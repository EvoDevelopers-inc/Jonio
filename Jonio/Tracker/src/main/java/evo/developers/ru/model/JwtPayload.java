package evo.developers.ru.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtPayload {
    private String pKeyBase64;
}
