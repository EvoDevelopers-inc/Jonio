package evo.developers.ru.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.util.Base64URL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.text.ParseException;
import java.util.UUID;

@Slf4j
@Service
public class KeyService {

    /*
    *
    * Check pubKey Client -> Ed25519
    *
    * */
    public boolean validatePubKey(String pubKeyJwk) {

        try {

            JWK jwk = JWK.parse(pubKeyJwk);

            if (!(jwk instanceof OctetKeyPair)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Key is not OctetKeyPair (Ed25519)");
            }

            OctetKeyPair pubKey = (OctetKeyPair) jwk;

            if (!Curve.Ed25519.equals(pubKey.getCurve())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Need Ed25519 public key");
            }

            return true;

        } catch (java.text.ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JWK format");
        }
    }

}
