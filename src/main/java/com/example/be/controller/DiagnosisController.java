package com.example.be.controller;

import com.example.be.dto.CommentDto;
import com.example.be.dto.DiagnosisResultDto;
import com.example.be.entity.Comment;
import com.example.be.entity.DiagnosisResult;
import com.example.be.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    // X-ray 이미지 업로드 및 AI 분석 요청
    @PostMapping
    public ResponseEntity<DiagnosisResult> requestDiagnosis(
            @RequestParam("patientId") Integer patientId,
            @RequestParam("file") MultipartFile xrayFile) throws IOException {

        DiagnosisResult result = diagnosisService.requestDiagnosis(patientId, xrayFile);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<DiagnosisResultDto> getDiagnosisResult(@PathVariable Integer resultId) {
        DiagnosisResultDto resultDto = diagnosisService.getDiagnosisResultById(resultId);
        return ResponseEntity.ok(resultDto);
    }

    // 의료진 소견 추가
    @PostMapping("/{resultId}/comments")
    public ResponseEntity<CommentDto> addOpinion( // 👈 반환 타입을 CommentDto로 변경
                                                  @PathVariable Integer resultId,
                                                  @RequestBody Map<String, String> payload) {

        String content = payload.get("content");
        CommentDto commentDto = diagnosisService.addDoctorOpinion(resultId, content);
        return ResponseEntity.ok(commentDto);
    }
}