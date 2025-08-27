package com.example.be.controller;

import com.example.be.dto.CommentDto;
import com.example.be.dto.DiagnosisResultDto;
import com.example.be.dto.PatientDto;
import com.example.be.entity.Comment;
import com.example.be.entity.DiagnosisResult;
import com.example.be.service.DiagnosisService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diagnosis")
@RequiredArgsConstructor
@Validated
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    // 신규 환자 등록, X-ray 업로드, AI 분석을 한 번에 처리하는 통합 API
    // 신규 환자 진단 시작 API
    @PostMapping(value = "/start/new-patient", consumes = "multipart/form-data")
    public ResponseEntity<PatientDto> startNewPatientDiagnosis( // 반환 타입을 PatientDto로 변경
                                                                @RequestPart("file") MultipartFile file,
                                                                // --- 필수 파라미터 ---
                                                                @RequestParam("name") String name,
                                                                @RequestParam("birthDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
                                                                @RequestParam("gender") @Pattern(regexp = "^[MF]$") String gender,
                                                                // --- 선택 파라미터 ---
                                                                @RequestParam(required = false) String patientCode,
                                                                @RequestParam(required = false) String bloodType,
                                                                @RequestParam(required = false) Float height,
                                                                @RequestParam(required = false) Float weight,
                                                                @RequestParam(required = false) String country
    ) throws IOException {
        PatientDto patientInfo = PatientDto.builder()
                .patientCode(patientCode)
                .name(name)
                .birthDate(birthDate)
                .gender(gender)
                .bloodType(bloodType)
                .height(height)
                .weight(weight)
                .country(country)
                .build();
        PatientDto result = diagnosisService.startDiagnosisForNewPatient(patientInfo, file); // 반환 타입을 PatientDto로 받음
        return ResponseEntity.ok(result);
    }

    // 기존 환자 진단 시작 API
    @PostMapping(value = "/start/existing-patient", consumes = "multipart/form-data")
    public ResponseEntity<PatientDto> startExistingPatientDiagnosis( // 반환 타입을 PatientDto로 변경
                                                                     @RequestPart("file") MultipartFile file,
                                                                     @RequestParam("patientId") Integer patientId
    ) throws IOException {
        PatientDto result = diagnosisService.startDiagnosisForExistingPatient(patientId, file);
        return ResponseEntity.ok(result);
    }

    //AI 진단 결과 조회
    @GetMapping("/{resultId}")
    public ResponseEntity<DiagnosisResultDto> getDiagnosisResult(@PathVariable Integer resultId) {
        DiagnosisResultDto resultDto = diagnosisService.getDiagnosisResultById(resultId);
        return ResponseEntity.ok(resultDto);
    }

    // 의료진 소견 추가
    @PostMapping("/{resultId}/comments")
    public ResponseEntity<CommentDto> addOpinion( // 반환 타입을 CommentDto로 변경
                                                  @PathVariable Integer resultId,
                                                  @RequestBody Map<String, String> payload) {

        String content = payload.get("content");
        CommentDto commentDto = diagnosisService.addDoctorOpinion(resultId, content);
        return ResponseEntity.ok(commentDto);
    }

    //특정 진단 보고서 삭제 API
    @DeleteMapping("/{resultId}")
    public ResponseEntity<Map<String, String>> deleteDiagnosisResult(@PathVariable Integer resultId) {
        diagnosisService.deleteDiagnosisResult(resultId);
        return ResponseEntity.ok(Map.of("message", "진단 보고서가 성공적으로 삭제되었습니다."));
    }
}