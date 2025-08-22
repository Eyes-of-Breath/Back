package com.example.be.dto;

import com.example.be.entity.XrayImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class XrayImageDto {

    private Integer imageId;
    private String imageUrl;
    private String fileName;
    private LocalDateTime uploadedAt;

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