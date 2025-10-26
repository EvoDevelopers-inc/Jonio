package evo.developers.ru.cryptography;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import evo.developers.ru.service.KeyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class KeyServiceTest {

    private KeyService keyService = new KeyService();

    @Test
    void validatePubKey_validKey_shouldReturnTrue() throws JOSEException {
        OctetKeyPair keyPair = new OctetKeyPairGenerator(Curve.Ed25519)
                .keyID(UUID.randomUUID().toString())
                .generate();

        System.out.println("Private JWK: " + keyPair.toJSONString());

        String validKey = keyPair.toPublicJWK().toJSONString();

        System.out.println("Public JWK: " + keyPair.toPublicJWK().toJSONString());

        boolean result = keyService.validatePubKey(validKey);
        assertTrue(result);
    }

    @Test
    void validatePubKey_invalidCurve_shouldThrow_errorKey() {
        String invalidKey = "qwertyuiop[asdfghjkl;'Zxcvbnm,./1234567890-=";
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> keyService.validatePubKey(invalidKey)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Invalid JWK format", ex.getReason());
    }

    @Test
    void validatePubKey_invalidCurve_shouldThrow_errorTypeKey() throws Exception {

        List<Curve> notAllowedCurves = List.of(
                Curve.Ed448,
                Curve.X25519,
                Curve.X448,
                Curve.P_256,
                Curve.P_384,
                Curve.P_521,
                Curve.SECP256K1
        );

        notAllowedCurves.forEach(notAllowedCurve -> {
            try {
                OctetKeyPair keyPair = new OctetKeyPairGenerator(notAllowedCurve)
                        .keyID(UUID.randomUUID().toString())
                        .generate();
                System.out.println("Private JWK: " + keyPair.toJSONString());

                String validKeyNotAllowedCurve = keyPair.toPublicJWK().toJSONString();

                System.out.println("Public JWK: " + keyPair.toPublicJWK().toJSONString());

                boolean result = keyService.validatePubKey(validKeyNotAllowedCurve);

                if (result) {
                    fail("ValidatePubKey returned true for forbidden curve: %s".formatted(notAllowedCurve));
                }

            } catch (Exception e) {
                System.out.println("Expected exception for curve " + notAllowedCurve + ": " + e.getMessage());
            }

        });
    }
}
