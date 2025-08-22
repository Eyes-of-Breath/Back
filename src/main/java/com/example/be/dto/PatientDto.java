package com.example.be.dto;

import com.example.be.entity.Patient;
import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List; // List import 추가
import java.util.stream.Collectors; // Collectors import 추가

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDto {

    private Integer patientId;
    private String patientCode; // 환자 고유 ID
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String bloodType;
    private Float height;
    private Float weight;
    private String anonymousId;
    private String country;
    private String currentMedication;
    private LocalDateTime lastAnalysisAt;
    private LocalDate lastVisitDate;
    private String specialNotes;
    private Integer memberId;

    private List<XrayImageDto> xrayImages;

    // Patient 엔티티를 PatientDto로 변환하는 정적(static) 메소드
    public static PatientDto fromEntity(Patient patient) {
        return PatientDto.builder()
                .patientId(patient.getPatientId())
                .patientCode(patient.getPatientCode())
                .name(patient.getName())
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender())
                .bloodType(patient.getBloodType())
                .height(patient.getHeight())
                .weight(patient.getWeight())
                .anonymousId(patient.getAnonymousId())
                .country(patient.getCountry())
                .currentMedication(patient.getCurrentMedication())
                .lastAnalysisAt(patient.getLastAnalysisAt())
                .lastVisitDate(patient.getLastVisitDate())
                .specialNotes(patient.getSpecialNotes())
                .memberId(patient.getMember() != null ? patient.getMember().getId() : null)
                .build();
    }
}