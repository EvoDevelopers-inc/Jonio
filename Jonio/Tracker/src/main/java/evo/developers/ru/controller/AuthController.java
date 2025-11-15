package evo.developers.ru.controller;

import com.nimbusds.jose.jwk.JWKSet;
import evo.developers.ru.dto.RequestAuthJwt;
import evo.developers.ru.dto.RequestRefreshJwt;
import evo.developers.ru.dto.ResponseAuthJwt;
import evo.developers.ru.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/jwt/create")
    public ResponseEntity<ResponseAuthJwt> auth(@RequestBody RequestAuthJwt auth) {
        return ResponseEntity.ok(authService.auth(auth));
    }

    @PostMapping("/jwt/refresh")
    public ResponseEntity<ResponseAuthJwt> refresh(@RequestBody RequestRefreshJwt refreshJwt) {
        return ResponseEntity.ok(authService.refresh(refreshJwt));
    }

}
