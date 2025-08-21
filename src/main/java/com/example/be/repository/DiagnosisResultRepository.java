package com.example.be.repository;

import com.example.be.entity.DiagnosisResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisResultRepository extends JpaRepository<DiagnosisResult, Integer> {
}