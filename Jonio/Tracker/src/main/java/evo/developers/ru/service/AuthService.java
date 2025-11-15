package evo.developers.ru.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.util.Base64URL;
import evo.developers.ru.controller.AuthController;
import evo.developers.ru.dto.RequestAuthJwt;
import com.nimbusds.jose.jwk.Curve;
import evo.developers.ru.dto.RequestRefreshJwt;
import evo.developers.ru.dto.ResponseAuthJwt;
import evo.developers.ru.model.JWT;
import evo.developers.ru.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final KeyService keyService;

    private final Base64Service helperBase64;

    private final ClientService clientService;

    private final JwtService jwtService;


    public ResponseAuthJwt auth(RequestAuthJwt requestAuth)  {

        String jwk = helperBase64.decode(requestAuth.getPubKeyBase64());
        keyService.validatePubKey(jwk);

        clientService.validationPasswordAndLogin(requestAuth);

        String jOnioID = clientService.computeIdHmac(requestAuth.getUsername(), requestAuth.getPassword());

        JWT jwt = jwtService.createJWT(jOnioID, jwk, List.of(Role.USER, Role.Admin, Role.Developer));

        return ResponseAuthJwt.builder()
                .token(jwt.getToken())
                .tokenRefresh(jwt.getRefreshToken())
                .build();
    }

    public ResponseAuthJwt refresh(RequestRefreshJwt payload) {

        String refreshToken = payload.getRefreshToken();
        String jwt = payload.getToken();

        validateRefreshableJwt(refreshToken);
        validateJwt(jwt);

        return ResponseAuthJwt.builder().build();

    }


    protected void validateRefreshableJwt(String jwtRefresh) {
        if(!jwtService.isTokenValid(jwtRefresh)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token");
        }
    }

    protected void validateJwt(String jwt) {
        if(!jwtService.isTokenSignValid(jwt) || ! jwtService.isTokenExpired(jwt)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The token does not need to be updated or has an error in the signature");
        }

    }



}
