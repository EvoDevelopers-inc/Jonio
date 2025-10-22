package evo.developers.ru.dto;

import lombok.Data;

@Data
public class RequestAuthJwt {
    private final String username;
    private final String password;
}
