// src/main/java/com/example/be/service/ai/AiClient.java
package com.example.be.service.ai;

import java.util.Map;
import java.util.Objects;

import com.example.be.dto.response.AiResponseDto; // 네가 갖고있는 DTO 사용
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.model-server.url}")
    private String modelServerUrl;

    public AiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiResponseDto predict(String imageUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("imageUrl", imageUrl);

        HttpEntity<Map<String, String>> req = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<AiResponseDto> resp =
                    restTemplate.postForEntity(modelServerUrl, req, AiResponseDto.class);
            return Objects.requireNonNull(resp.getBody(), "AI response is null");
        } catch (RestClientException e) {
            throw new RuntimeException("AI 서버 호출 실패: " + e.getMessage(), e);
        }
    }
}
