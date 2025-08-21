package com.example.be.entity;

import com.example.be.dto.PatientDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Integer patientId; // DB 내부에서 사용하는 자동 증가 ID

    @Column(name = "patient_code", nullable = false, unique = true)
    private String patientCode; // 사용자가 입력하고 검색에 사용할 고유 ID

    @Column(name = "patient_name", nullable = false)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    // 이하 필드는 선택값 (nullable = true)
    @Column(name = "gender")
    private String gender;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "height")
    private Float height;

    @Column(name = "weight")
    private Float weight;

    @Column(name = "anonymous_id")
    private String anonymousId;

    @Column(name = "country")
    private String country;

    @Column(name = "current_medication")
    private String currentMedication;

    @Column(name = "last_analysis_at")
    private LocalDateTime lastAnalysisAt;

    @Column(name = "last_visit_date")
    private LocalDate lastVisitDate;

    @Column(name = "special_notes")
    private String specialNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    // 환자 정보 수정을 위한 메소드 추가
    public void update(PatientDto dto) {
        this.patientCode = dto.getPatientCode();
        this.name = dto.getName();
        this.birthDate = dto.getBirthDate();
        this.gender = dto.getGender();
        this.bloodType = dto.getBloodType();
        this.height = dto.getHeight();
        this.weight = dto.getWeight();
        this.country = dto.getCountry();
        // 필요에 따라 다른 필드들도 여기에 추가
    }


}