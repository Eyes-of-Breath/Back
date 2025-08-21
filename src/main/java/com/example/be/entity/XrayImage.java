package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "xray_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class XrayImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Integer imageId; // Long을 Integer로 수정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 필드 이름은 firebaseUrl로 유지하되, DB 컬럼명을 명시
    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "taken_at")
    private LocalDate takenAt;

    @Column(name = "resolution")
    private String resolution;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

}