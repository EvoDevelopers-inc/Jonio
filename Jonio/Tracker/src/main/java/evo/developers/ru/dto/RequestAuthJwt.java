package evo.developers.ru.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestAuthJwt {
    private String clientHash;
    private String pubKeyBase64; //example -> {"kty":"OKP","crv":"X25519","kid":"7acb7551-5051-42c7-9d21-5e818c657993","x":"H-JxLcmFfZtJss4EAN52EIMRh0gedhDLnr06VQ8Uyg8"}
}
