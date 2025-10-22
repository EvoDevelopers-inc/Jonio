package evo.developers.ru.controller;

import evo.developers.ru.dto.RequestAuthJwt;
import evo.developers.ru.dto.ResponseAuthJwt;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<ResponseAuthJwt> auth(@RequestBody RequestAuthJwt auth) {
        return ResponseEntity.ok(new ResponseAuthJwt("", ""));
    }
}
