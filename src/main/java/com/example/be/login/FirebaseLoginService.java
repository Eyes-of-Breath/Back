package com.example.be.login;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FirebaseLoginService {

    @Value("${firebase.api-key}")
    private String firebaseApiKey;

    public String loginWithEmailAndPassword(String email, String password) {
        String firebaseUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(firebaseUrl, HttpMethod.POST, request, Map.class);
            Map body = response.getBody();
            return body != null ? (String) body.get("idToken") : null;
        } catch (Exception e) {
            return null; // 실패시 null 반환
        }
    }

    public String signupWithEmailAndPassword(String email, String password) {
        String firebaseUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + firebaseApiKey;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(firebaseUrl, HttpMethod.POST, request, Map.class);
            Map body = response.getBody();
            return body != null ? (String) body.get("idToken") : null;
        } catch (Exception e) {
            return null; // 실패 시 null 반환
        }
    }
}