package evo.developers.ru.jonio.client.core.base;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.OctetKeyPair;

public interface IJwkPairFactory {

    OctetKeyPair generateKeyPair() throws JOSEException;
    String getPrivateJwk(OctetKeyPair  keyPair) throws JOSEException;
    String getPublicJwk(OctetKeyPair  keyPair);
}
