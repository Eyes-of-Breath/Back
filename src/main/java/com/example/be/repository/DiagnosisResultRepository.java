package com.example.be.repository;

import com.example.be.entity.DiagnosisResult;
import com.example.be.entity.XrayImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiagnosisResultRepository extends JpaRepository<DiagnosisResult, Integer> {
    Optional<DiagnosisResult> findByXrayImage(XrayImage xrayImage); //XrayImage 엔티티로 진단 결과를 찾
}