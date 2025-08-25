package com.example.be.service;

import com.example.be.dto.CommentDto;
import com.example.be.dto.DiagnosisResultDto;
import com.example.be.dto.PatientDto;
import com.example.be.dto.XrayImageDto;
import com.example.be.dto.response.AiResponseDto;
import com.example.be.entity.*;
import com.example.be.repository.*;
import com.example.be.service.ai.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosisService {

    private final FirebaseService firebaseService;
    private final MemberRepository memberRepository;
    private final PatientRepository patientRepository;
    private final XrayImageRepository xrayImageRepository;
    private final DiagnosisResultRepository diagnosisResultRepository;
    private final AiClient aiClient;                // ✅ AiClient 주입
    private final CommentRepository commentRepository;

    // 신규 환자 진단 시작 (메서드 이름 유지)
    public PatientDto startDiagnosisForNewPatient(PatientDto patientDto, MultipartFile xrayFile) throws IOException {
        Member member = getCurrentMember();

        String patientCode = patientDto.getPatientCode();
        if (patientCode == null || patientCode.isBlank()) {
            patientCode = "P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (patientRepository.existsByPatientCode(patientCode)) {
            throw new IllegalArgumentException("이미 사용 중인 환자 ID입니다: " + patientCode);
        }

        Patient newPatient = Patient.builder()
                .patientCode(patientCode)
                .name(patientDto.getName())
                .birthDate(patientDto.getBirthDate())
                .gender(patientDto.getGender())
                .member(member)
                .build();
        Patient savedPatient = patientRepository.save(newPatient);

        DiagnosisResultDto diagnosisResultDto = processDiagnosis(savedPatient, member, xrayFile);

        PatientDto finalPatientDto = PatientDto.fromEntity(savedPatient);
        finalPatientDto.setDiagnosisResult(diagnosisResultDto);
        finalPatientDto.setXrayImages(Collections.singletonList(
                XrayImageDto.builder()
                        .imageId(diagnosisResultDto.getImageId())
                        .imageUrl(diagnosisResultDto.getImageUrl())
                        .build()
        ));
        return finalPatientDto;
    }

    // 기존 환자 진단 시작 (메서드 이름 유지)
    public PatientDto startDiagnosisForExistingPatient(Integer patientId, MultipartFile xrayFile) throws IOException {
        Member member = getCurrentMember();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다."));

        DiagnosisResultDto diagnosisResultDto = processDiagnosis(patient, member, xrayFile);

        PatientDto finalPatientDto = PatientDto.fromEntity(patient);
        finalPatientDto.setDiagnosisResult(diagnosisResultDto);
        finalPatientDto.setXrayImages(Collections.singletonList(
                XrayImageDto.builder()
                        .imageId(diagnosisResultDto.getImageId())
                        .imageUrl(diagnosisResultDto.getImageUrl())
                        .build()
        ));
        return finalPatientDto;
    }

    // 공통 진단 처리 로직 (메서드 이름 유지)
    private DiagnosisResultDto processDiagnosis(Patient patient, Member member, MultipartFile xrayFile) throws IOException {
        // 1) 원본 업로드 (Firebase Service 사용)
        String imageUrl = firebaseService.uploadFile(xrayFile, "xray-images");

        // 2) 이미지 메타 저장
        XrayImage xrayImage = XrayImage.builder()
                .patient(patient)
                .member(member)
                .imageUrl(imageUrl)
                .fileName(xrayFile.getOriginalFilename())
                .fileSize((int) xrayFile.getSize())
                .build();
        XrayImage savedXrayImage = xrayImageRepository.save(xrayImage);

        // 3) AI 서버 호출
        AiResponseDto aiResponse = callAiServer(imageUrl);

        // 4) 결과 저장 (널 방어)
        Float prob = (aiResponse.getProbability() != null) ? aiResponse.getProbability() : 0f;
        // gradcam 공개 URL이 있으면 그걸, 없으면 내부 경로 사용
//        String camPath = aiResponse.getGradcamImagePath() != null ? aiResponse.getGradcamImagePath() : "";
        String camUrl  = firstNonBlank(aiResponse.getGradcamUrl());
        if (isBlank(camUrl)) {
            camUrl = gsToFirebaseHttps(aiResponse.getGradcamImagePath());
        }
        String camHttps = firstNonBlank(camUrl);
        if (isBlank(camHttps)) camHttps = "";


        // 새로추가
        String top1Label = null, top2Label = null, top3Label = null;
        Float top1Prob = null, top2Prob = null, top3Prob = null;

        if (aiResponse.getTop3() != null && !aiResponse.getTop3().isEmpty()) {
            int n = aiResponse.getTop3().size();
            if (n >= 1) {
                top1Label = aiResponse.getTop3().get(0).getLabel();
                top1Prob  = aiResponse.getTop3().get(0).getProbability();
            }
            if (n >= 2) {
                top2Label = aiResponse.getTop3().get(1).getLabel();
                top2Prob  = aiResponse.getTop3().get(1).getProbability();
            }
            if (n >= 3) {
                top3Label = aiResponse.getTop3().get(2).getLabel();
                top3Prob  = aiResponse.getTop3().get(2).getProbability();
            }
        }

        // top3가 비어있다면 최소한 top1은 예전 방식으로 채움
        if (top1Label == null) {
            top1Label = aiResponse.getPredictedDisease();
            top1Prob  = prob;
        }

        DiagnosisResult result = DiagnosisResult.builder()
                .xrayImage(savedXrayImage)
                .predictedDisease(aiResponse.getPredictedDisease())
                .probability(prob)
                .gradcamImagePath(camHttps)
//                .gradcamImagePath(camPath)
                .top1Disease(top1Label)
                .top1Probability(top1Prob)
                .top2Disease(top2Label)
                .top2Probability(top2Prob)
                .top3Disease(top3Label)
                .top3Probability(top3Prob)
                .build();

        DiagnosisResult savedResult = diagnosisResultRepository.save(result);
        return DiagnosisResultDto.fromEntity(savedResult);
    }

    // 메서드 이름 유지: 내부 구현만 AiClient 위임으로 교체
    private AiResponseDto callAiServer(String imageUrl) {
        AiResponseDto resp = aiClient.predict(imageUrl);
        if (resp == null) {
            throw new IllegalStateException("AI 서버 응답이 비어있습니다.");
        }
        return resp;
    }

    @Transactional(readOnly = true)
    public DiagnosisResultDto getDiagnosisResultById(Integer resultId) {
        return diagnosisResultRepository.findById(resultId)
                .map(DiagnosisResultDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("진단 결과를 찾을 수 없습니다."));
    }

    public CommentDto addDoctorOpinion(Integer resultId, String content) {
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

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    private static String firstNonBlank(String... options) {
        if (options == null) return null;
        for (String s : options) {
            if (!isBlank(s)) return s;
        }
        return null;
    }
    private static String gsToFirebaseHttps(String gsUrl) {
        if (isBlank(gsUrl)) return null;
        final String prefix = "gs://";
        if (!gsUrl.startsWith(prefix)) return gsUrl; // 이미 https면 그대로
        String rest = gsUrl.substring(prefix.length()); // bucket/path...
        int slash = rest.indexOf('/');
        if (slash < 0) return null; // 형식 오류
        String bucket = rest.substring(0, slash);
        String objectPath = rest.substring(slash + 1);
        String encoded = java.net.URLEncoder.encode(objectPath, java.nio.charset.StandardCharsets.UTF_8);
        return "https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/" + encoded + "?alt=media";
    }

}
