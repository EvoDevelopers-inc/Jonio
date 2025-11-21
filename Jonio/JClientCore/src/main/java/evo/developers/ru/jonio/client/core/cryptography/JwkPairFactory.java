package evo.developers.ru.jonio.client.core.cryptography;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import evo.developers.ru.jonio.client.core.base.IJwkPairFactory;

import java.util.UUID;

public class JwkPairFactory implements IJwkPairFactory {

    @Override
    public OctetKeyPair generateKeyPair() throws JOSEException {
        return new OctetKeyPairGenerator(Curve.Ed25519)
                .keyID(UUID.randomUUID().toString())
                .generate();
    }

    @Override
    public String getPrivateJwk(OctetKeyPair  keyPair) {
        return keyPair.toJSONString();
    }

    @Override
    public String getPublicJwk(OctetKeyPair  keyPair) {
        return keyPair.toPublicJWK().toJSONString();
    }
}
