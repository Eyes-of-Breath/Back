package com.example.be.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiResponseDto {
    private String predictedDisease;
    private Float probability;
    private String gradcamUrl;
}