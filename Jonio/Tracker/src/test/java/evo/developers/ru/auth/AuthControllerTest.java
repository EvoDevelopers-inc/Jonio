package evo.developers.ru.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import evo.developers.ru.controller.AuthController;
import evo.developers.ru.dto.RequestAuthJwt;
import evo.developers.ru.dto.ResponseAuthJwt;
import evo.developers.ru.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthControllerTest {
    /*
    static {
        authService = new AuthService(new KeyService(), new Base64Service(), new ClientService(), new JwtService(ed25519KeyPair(), new JsonService(), new JwtVersionControlService(null)));
    }

    public static final AuthService authService;

    @Test
    void testAuth() throws Exception {
        RequestAuthJwt requestAuthJwt = new RequestAuthJwt();
        requestAuthJwt.setUsername("test333");
        requestAuthJwt.setPassword("123333");


        OctetKeyPair keyPair = new OctetKeyPairGenerator(Curve.Ed25519)
                .keyID(UUID.randomUUID().toString())
                .generate();
        String pubKeyBase64 = Base64.getEncoder().encodeToString(keyPair.toPublicJWK().toJSONString().getBytes(StandardCharsets.UTF_8));
        requestAuthJwt.setPubKeyBase64(pubKeyBase64);
        ResponseAuthJwt response = authService.auth(requestAuthJwt);

        assertTrue(!response.getToken().isEmpty());
        assertTrue(!response.getTokenRefresh().isEmpty());
        System.out.println(response);

    }

    private static OctetKeyPair ed25519KeyPair()  {
        try {
            return new OctetKeyPairGenerator(Curve.Ed25519)
                    .keyID(UUID.randomUUID().toString())
                    .generate();
        }catch (Exception e){
           new RuntimeException(e);
        }

        return null;
    }*/

}
