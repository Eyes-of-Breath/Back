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
    private String gradcamUrl;
    private LocalDateTime createdAt;
    private Integer patientId;

    private String top1Disease;
    private Float top1Probability;
    private String top2Disease;
    private Float top2Probability;
    private String top3Disease;
    private Float top3Probability;

    public static DiagnosisResultDto fromEntity(DiagnosisResult entity) {
        return DiagnosisResultDto.builder()
                .resultId(entity.getResultId())
                .imageId(entity.getXrayImage().getImageId())
                .imageUrl(entity.getXrayImage().getImageUrl())
                .predictedDisease(entity.getPredictedDisease())
                .probability(entity.getProbability())
                .gradcamImagePath(entity.getGradcamImagePath())
                .createdAt(entity.getCreatedAt())
                .patientId(entity.getXrayImage().getPatient().getPatientId())
                .top1Disease(entity.getTop1Disease())
                .top1Probability(entity.getTop1Probability())
                .top2Disease(entity.getTop2Disease())
                .top2Probability(entity.getTop2Probability())
                .top3Disease(entity.getTop3Disease())
                .top3Probability(entity.getTop3Probability())
                .build();
    }
}
