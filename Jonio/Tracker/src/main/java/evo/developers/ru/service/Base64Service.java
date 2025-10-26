package evo.developers.ru.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

@Service
public class Base64Service {

    public String decode(String base64) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Base64 format");
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T decode(String base64, Class<T> clazz) {
        try {

            byte[] decodedBytes = Base64.getDecoder().decode(base64);

            return objectMapper.readValue(decodedBytes, clazz);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Base64 format", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON content", e);
        }
    }

    public String encode(String base64) {
        try {
            byte[] encodedBytes = Base64.getEncoder().encode(base64.getBytes());
            return new String(encodedBytes);
        }catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Base64 format", e);
        }
    }

}
