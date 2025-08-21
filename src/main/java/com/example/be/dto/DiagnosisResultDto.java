package com.example.be.dto;

import com.example.be.entity.DiagnosisResult;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class DiagnosisResultDto {
    private Integer resultId;
    private Integer imageId;
    private String imageUrl;
    private String predictedDisease;
    private Float probability;
    private String gradcamImagePath;
    private LocalDateTime createdAt;

    public static DiagnosisResultDto fromEntity(DiagnosisResult entity) {
        return DiagnosisResultDto.builder()
                .resultId(entity.getResultId())
                .imageId(entity.getXrayImage().getImageId())
                .imageUrl(entity.getXrayImage().getImageUrl())
                .predictedDisease(entity.getPredictedDisease())
                .probability(entity.getProbability())
                .gradcamImagePath(entity.getGradcamImagePath())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}