package evo.developers.ru.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.jwk.OctetKeyPair;
import evo.developers.ru.model.JWT;
import evo.developers.ru.model.JwtPayload;
import evo.developers.ru.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final OctetKeyPair keyPair;
    private final JsonService jsonService;


    public JWT createJWT(String jOnioID, String pKey, List<Role> listRole)
    {
        return JWT.builder()
                .token(createToken(jOnioID, pKey, listRole))
                .refreshToken(createRefreshToken(jOnioID))
                .build();
    }

    public String createToken(String jOnioID, String pKey, List<Role> listRole) {

        try {
            JWSSigner signer = new Ed25519Signer(keyPair);

            String nonce = UUID.randomUUID().toString();
            String jti = UUID.randomUUID().toString();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(jOnioID)
                    .issuer(jsonService.toJson(JwtPayload.builder().pKey(pKey).build()))
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 3600_000))
                    .claim("roles", listRole)
                    .claim("nonce", nonce)
                    .jwtID(jti)
                    .build();

            System.out.println("Nonce: " + nonce + ", JTI: " + jti);

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                    .keyID(keyPair.getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(signer);

            return jwt.serialize();
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JWT create error", e);
        }

    }

    public String createRefreshToken(String userId) {
        try {
            JWSSigner signer = new Ed25519Signer(keyPair);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)) // 7 day
                    .claim("type", "refresh")
                    .claim("nonce", UUID.randomUUID().toString())
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                    .keyID(keyPair.getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(signer);

            return jwt.serialize();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Refresh JWT create error", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationTime = claims.getExpirationTime();

            if (expirationTime == null) {
                return true;
            }

            return expirationTime.before(new Date());
        } catch (ParseException e) {

            return true;
        }
    }

    public boolean isTokenValid(String token) {

        if (!isTokenSignValid(token)) {
            return false;
        }

        return !isTokenExpired(token);
    }

    public boolean isTokenSignValid(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new Ed25519Verifier(keyPair.toPublicJWK());

            return signedJWT.verify(verifier);


        } catch (Exception e) {
            return false;
        }
    }

}
