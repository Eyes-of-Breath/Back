package com.example.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "diagnosis_result")
public class DiagnosisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Integer resultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private XrayImage xrayImage; // 어떤 이미지에 대한 결과인지 연결

    @Column(name = "predicted_disease")
    private String predictedDisease; // 예측된 질병명

    @Column(name = "probability")
    private Float probability; // 예측 확률

    @Column(name = "gradcam_image_path")
    private String gradcamImagePath; // Grad-CAM 결과 이미지 경로

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}