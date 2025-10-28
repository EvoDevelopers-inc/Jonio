package evo.developers.ru.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetKeyPair;
import java.util.Map;

@RestController
public class JwksController {
    private final JWKSet jwkSet;

    public JwksController(OctetKeyPair keyPair) {
        this.jwkSet = new JWKSet(keyPair.toPublicJWK());
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        return jwkSet.toJSONObject();
    }
}
