package com.example.be.service;

import com.example.be.dto.CommentDto;
import com.example.be.dto.DiagnosisResultDto;
import com.example.be.entity.*;
import com.example.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final FirebaseService firebaseService;
    private final MemberRepository memberRepository;
    private final PatientRepository patientRepository;
    private final XrayImageRepository xrayImageRepository;
    private final DiagnosisResultRepository diagnosisResultRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public DiagnosisResult requestDiagnosis(Integer patientId, MultipartFile xrayFile) throws IOException {
        // 1. 사용자 및 환자 정보 조회
        Member member = getCurrentMember();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다."));

        // 2. Firebase에 X-ray 이미지 업로드
        String imageUrl = firebaseService.uploadFile(xrayFile);

        // 3. XrayImage 정보 DB에 저장
        XrayImage xrayImage = XrayImage.builder()
                .patient(patient)
                .member(member)
                .imageUrl(imageUrl)
                .fileName(xrayFile.getOriginalFilename())
                .fileSize((int) xrayFile.getSize()) // 파일 사이즈 추가
                .build();
        xrayImageRepository.save(xrayImage);

        // 4. AI 모델 호출 (시뮬레이션)
        String predictedDisease = "폐렴 (Pneumonia)";
        float probability = 0.925f;
        String gradcamUrl = "path/to/gradcam/image_in_firebase.jpg";

        // 5. AI 진단 결과 DB에 저장
        DiagnosisResult result = DiagnosisResult.builder()
                .xrayImage(xrayImage)
                .predictedDisease(predictedDisease)
                .probability(probability)
                .gradcamImagePath(gradcamUrl)
                .build();

        return diagnosisResultRepository.save(result);
    }

    @Transactional
    public CommentDto addDoctorOpinion(Integer resultId, String content) { // 👈 반환 타입을 CommentDto로 변경
        Member member = getCurrentMember();
        DiagnosisResult result = diagnosisResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("진단 결과를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .diagnosisResult(result)
                .member(member)
                .content(content)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentDto.fromEntity(savedComment);
    }

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public DiagnosisResultDto getDiagnosisResultById(Integer resultId) {
        return diagnosisResultRepository.findById(resultId)
                .map(DiagnosisResultDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("진단 결과를 찾을 수 없습니다."));
    }
}