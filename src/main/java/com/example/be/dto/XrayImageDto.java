package com.example.be.dto;

import com.example.be.entity.XrayImage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class XrayImageDto {

    private Integer imageId;
    private String imageUrl;
    private String fileName;
    private LocalDateTime uploadedAt;
    private DiagnosisResultDto diagnosisResult;

    // XrayImage 엔티티를 DTO로 변환하는 정적 메소드
    public static XrayImageDto fromEntity(XrayImage entity) {
        return XrayImageDto.builder()
                .imageId(entity.getImageId())
                .imageUrl(entity.getImageUrl())
                .fileName(entity.getFileName())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
}