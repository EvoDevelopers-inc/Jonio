package evo.developers.ru.service;

import evo.developers.ru.dto.RequestAuthJwt;
import evo.developers.ru.dto.RequestRefreshJwt;
import evo.developers.ru.dto.ResponseAuthJwt;
import evo.developers.ru.entity.JonioAddress;
import evo.developers.ru.entity.JwtVersionControl;
import evo.developers.ru.entity.User;
import evo.developers.ru.jpa.UserRepository;
import evo.developers.ru.model.Jwt;
import evo.developers.ru.model.JwtPayload;
import evo.developers.ru.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final KeyService keyService;

    private final Base64Service helperBase64;

    private final ClientService clientService;

    private final JwtService jwtService;

    private final JwtVersionControlService jwtVersionControlService;

    private final UserRepository userRepository;


    public ResponseAuthJwt auth(RequestAuthJwt requestAuth)  {

        String jwk = helperBase64.decode(requestAuth.getPubKeyBase64());
        System.out.println("jwk: " + jwk);
        keyService.validatePubKey(jwk);

        clientService.validationHashClient(requestAuth);

        String jOnioID = clientService.computeIdHmac(requestAuth.getClientHash());

        Optional<User> userOptional = userRepository.findById(jOnioID);

        JwtVersionControl versionControl = jwtVersionControlService.generateRandomVersion(new JwtVersionControl());

        if (userOptional.isEmpty()) {
            User newUser = new User();

            newUser.setId(jOnioID);
            newUser.setMaxVersionControl(List.of(versionControl));

        }else{
            User user = userOptional.get();
            user.setMaxVersionControl(List.of(versionControl));
        }

        String jwkValidBase64 = helperBase64.encode(jwk);
        Jwt jwt = jwtService.createJwtTokenAndRefresh(jOnioID, jwkValidBase64, List.of(Role.USER, Role.Admin, Role.Developer), versionControl.getIncrement());

        return ResponseAuthJwt.builder()
                .token(jwt.getToken())
                .tokenRefresh(jwt.getRefreshToken())
                .build();
    }

    public ResponseAuthJwt refresh(RequestRefreshJwt requestRefreshJwt) {

        String refreshToken = requestRefreshJwt.getRefreshToken();
        String jwt = requestRefreshJwt.getToken();

        validateRefreshableJwt(refreshToken);
        validateJwt(jwt);

        User user = getUserByJwt(jwt);

        long newJwtVersionId = jwtVersionControlService.plusPlusVersion(user);

        JwtPayload jwtPayload = jwtService.getJwtBody(jwt);

        String newToken = jwtService.createToken(user.getId(), jwtPayload.getPKeyBase64(), List.of(Role.USER, Role.Admin, Role.Developer), newJwtVersionId);

        return ResponseAuthJwt.builder()
                .tokenRefresh(refreshToken)
                .token(newToken)
                .build();
    }

    protected User getUserByJwt(String jwt) {

        Optional<User> userOptional = userRepository.findById(jwtService.getSubjectValidated(jwt));

        if(userOptional.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        return userOptional.get();
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
