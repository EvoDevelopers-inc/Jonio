package evo.developers.ru.model;

import lombok.Getter;

@Getter
public enum Role {

    USER("USER"),
    BetaTester("BetaTester"),
    Developer("DEVELOPER"),
    Admin("ADMIN"),
    Creator("Creator")

    ;

    Role(String role) {
        this.role = role;
    }

    private final String role;
}
