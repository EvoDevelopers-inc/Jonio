package evo.developers.ru.config;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class KeyConfig {

    @Bean
    public OctetKeyPair ed25519KeyPair() throws Exception {
        return new OctetKeyPairGenerator(Curve.Ed25519)
                .keyID(UUID.randomUUID().toString())
                .generate();
    }
}
