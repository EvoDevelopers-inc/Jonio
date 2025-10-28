package evo.developers.ru.service;

import evo.developers.ru.dto.RequestAuthJwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class ClientService {

    @Value("${jonio.pepper}")
    private String pepper = "key";

    public String computeIdHmac(String login, String password) {

        try {
            String data = login + ":" + password;
            byte[] key = pepper.getBytes(StandardCharsets.UTF_8);

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
            mac.init(keySpec);
            byte[] macBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(macBytes);

        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generate JOnio Id");
        }

    }

    protected void validationPasswordAndLogin(RequestAuthJwt requestAuth)
    {
        String password = requestAuth.getPassword();
        String username = requestAuth.getUsername();

        if (password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (username == null || username.length() < 4) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

    }
}
