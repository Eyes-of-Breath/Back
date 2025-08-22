package com.example.be.repository;

import com.example.be.entity.XrayImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface XrayImageRepository extends JpaRepository<XrayImage, Integer> {
    // 특정 환자 ID에 속한 모든 X-ray 이미지를 조회하는 메소드
    List<XrayImage> findAllByPatient_PatientId(Integer patientId);
}

