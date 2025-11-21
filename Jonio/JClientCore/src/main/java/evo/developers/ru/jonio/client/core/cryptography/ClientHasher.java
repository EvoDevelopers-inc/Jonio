package evo.developers.ru.jonio.client.core.cryptography;

import evo.developers.ru.jonio.client.core.base.IClientHasher;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientErrorGenerationHashAuth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ClientHasher implements IClientHasher {
    @Override
    public String computeClientHash(String login, String password) {
        try {
            String input = login + ":" + password;

            int iterations = 100_000;
            int keyLength = 256;

            PBEKeySpec spec = new PBEKeySpec(
                    input.toCharArray(),
                    login.getBytes(StandardCharsets.UTF_8),
                    iterations,
                    keyLength
            );

            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = skf.generateSecret(spec).getEncoded();

            return Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);

        } catch (Exception e) {
            throw new JOnioClientErrorGenerationHashAuth("Error generating client hash");
        }
    }
}
