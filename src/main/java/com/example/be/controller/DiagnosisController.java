package com.example.be.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@Tag(name = "Chest X-ray Diagnosis", description = "AI 진단 기능 관련 API")
@RestController
@RequestMapping("/api/v1/diagnosis")
public class DiagnosisController {

    @Operation(summary = "X-ray 업로드", description = "DICOM 또는 이미지 파일을 업로드하여 AI 분석 요청")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadXray(
            @Parameter(description = "X-ray 이미지 파일 (jpg/png/dcm 등)")
            @RequestPart MultipartFile file
    ) {
        // 실제로는 여기서 DICOM 처리 및 AI 모델 호출
        return ResponseEntity.ok(Map.of("message", "X-ray 업로드 성공", "filename", file.getOriginalFilename()));
    }

    @Operation(summary = "AI 진단 결과", description = "업로드된 X-ray에 대한 AI 분석 결과를 반환")
    @GetMapping("/result")
    public ResponseEntity<?> getResult() {
        // 실제로는 DB나 Redis에서 분석 결과 조회
        return ResponseEntity.ok(Map.of(
                "patientId", "P001",
                "disease", "폐렴(Pneumonia)",
                "confidence", 0.94,
                "heatmapUrl", "/images/heatmap_p001.png"
        ));
    }
}
