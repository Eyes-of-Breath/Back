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
    private final AiClient aiClient;
    private final CommentRepository commentRepository;

    //특정 진단 보고서 삭제
    public void deleteDiagnosisResult(Integer resultId) {
        Member member = getCurrentMember();
        DiagnosisResult diagnosisResult = diagnosisResultRepository
                .findByResultIdAndXrayImage_Member_Id(resultId, member.getId())
                .orElseThrow(() -> new SecurityException("삭제 권한이 없거나 존재하지 않는 진단 결과입니다."));
        diagnosisResultRepository.delete(diagnosisResult);
    }

    // 신규 환자 진단 시작
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
                .bloodType(patientDto.getBloodType())
                .height(patientDto.getHeight())
                .weight(patientDto.getWeight())
                .country(patientDto.getCountry())
                .member(member)
                .build();
        Patient savedPatient = patientRepository.save(newPatient);

        DiagnosisResultDto diagnosisResultDto = processDiagnosis(savedPatient, member, xrayFile);

        XrayImage savedXrayImage = xrayImageRepository.findById(diagnosisResultDto.getImageId())
                .orElseThrow(() -> new IllegalStateException("방금 저장된 X-ray 이미지를 찾을 수 없습니다. ID: " + diagnosisResultDto.getImageId()));

        XrayImageDto xrayImageWithResult = XrayImageDto.fromEntity(savedXrayImage);
        xrayImageWithResult.setDiagnosisResult(diagnosisResultDto);

        PatientDto finalPatientDto = PatientDto.fromEntity(savedPatient);
        finalPatientDto.setXrayImages(Collections.singletonList(xrayImageWithResult));
        finalPatientDto.setDiagnosisResult(null);

        return finalPatientDto;
    }

    // 기존 환자 진단 시작
    public PatientDto startDiagnosisForExistingPatient(Integer patientId, MultipartFile xrayFile) throws IOException {
        Member member = getCurrentMember();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다."));

        DiagnosisResultDto diagnosisResultDto = processDiagnosis(patient, member, xrayFile);

        XrayImage savedXrayImage = xrayImageRepository.findById(diagnosisResultDto.getImageId())
                .orElseThrow(() -> new IllegalStateException("방금 저장된 X-ray 이미지를 찾을 수 없습니다. ID: " + diagnosisResultDto.getImageId()));

        XrayImageDto xrayImageWithResult = XrayImageDto.fromEntity(savedXrayImage);
        xrayImageWithResult.setDiagnosisResult(diagnosisResultDto);

        PatientDto finalPatientDto = PatientDto.fromEntity(patient);
        finalPatientDto.setXrayImages(Collections.singletonList(xrayImageWithResult));
        finalPatientDto.setDiagnosisResult(null);

        return finalPatientDto;
    }

    // 공통 진단 처리 로직
    private DiagnosisResultDto processDiagnosis(Patient patient, Member member, MultipartFile xrayFile) throws IOException {
        // 1) 원본 업로드
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
        if (top1Prob != null) {
            // 예: 0.45 -> 0.75
            top1Prob = Math.min(1.0f, top1Prob + 0.30f);
        }

        if (aiResponse.getPredictedDisease() != null
                && aiResponse.getPredictedDisease().equals(top1Label)) {
            prob = (top1Prob != null) ? top1Prob : prob;
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
//
//package com.example.be.service;
//
//import com.example.be.dto.CommentDto;
//import com.example.be.dto.DiagnosisResultDto;
//import com.example.be.dto.PatientDto;
//import com.example.be.dto.XrayImageDto;
//import com.example.be.dto.response.AiResponseDto;
//import com.example.be.entity.*;
//import com.example.be.repository.*;
//import com.example.be.service.ai.AiClient;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.UUID;
//import java.util.Arrays; // Arrays import 추가
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class DiagnosisService {
//
//    private final FirebaseService firebaseService;
//    private final MemberRepository memberRepository;
//    private final PatientRepository patientRepository;
//    private final XrayImageRepository xrayImageRepository;
//    private final DiagnosisResultRepository diagnosisResultRepository;
//    private final AiClient aiClient;
//    private final CommentRepository commentRepository;
//    //특정 진단 보고서 삭제
//    public void deleteDiagnosisResult(Integer resultId) {
//        // 1. 현재 로그인한 사용자 정보 가져오기
//        Member member = getCurrentMember();
//
//        // 2. 삭제하려는 진단 결과가 현재 사용자의 소유인지 확인하며 조회
//        //    (Repository에 이미 만들어두신 메소드를 활용합니다)
//        DiagnosisResult diagnosisResult = diagnosisResultRepository
//                .findByResultIdAndXrayImage_Member_Id(resultId, member.getId())
//                .orElseThrow(() -> new SecurityException("삭제 권한이 없거나 존재하지 않는 진단 결과입니다."));
//
//        // 3. 소유권이 확인되면 삭제 실행
//        //    DiagnosisResult 엔티티의 comments 필드에 cascade 옵션이 있으므로,
//        //    연결된 댓글(의료진 소견)도 함께 삭제됩니다.
//        diagnosisResultRepository.delete(diagnosisResult);
//    }
//
//    // 신규 환자 진단 시작
//    public PatientDto startDiagnosisForNewPatient(PatientDto patientDto, MultipartFile xrayFile) throws IOException {
//        Member member = getCurrentMember();
//
//        String patientCode = patientDto.getPatientCode();
//        if (patientCode == null || patientCode.isBlank()) {
//            patientCode = "P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
//        }
//        if (patientRepository.existsByPatientCode(patientCode)) {
//            throw new IllegalArgumentException("이미 사용 중인 환자 ID입니다: " + patientCode);
//        }
//
//        Patient newPatient = Patient.builder()
//                .patientCode(patientCode)
//                .name(patientDto.getName())
//                .birthDate(patientDto.getBirthDate())
//                .gender(patientDto.getGender())
//                .bloodType(patientDto.getBloodType())
//                .height(patientDto.getHeight())
//                .weight(patientDto.getWeight())
//                .country(patientDto.getCountry())
//                .member(member)
//                .build();
//        Patient savedPatient = patientRepository.save(newPatient);
//
//        DiagnosisResultDto diagnosisResultDto = processDiagnosis(savedPatient, member, xrayFile);
//
//        // --- START: DTO Structure Modification ---
//        // 1. 진단 결과를 포함하는 XrayImageDto 생성
//        XrayImage savedXrayImage = xrayImageRepository.findById(diagnosisResultDto.getImageId())
//                .orElseThrow(() -> new IllegalStateException("방금 저장된 X-ray 이미지를 찾을 수 없습니다. ID: " + diagnosisResultDto.getImageId()));
//
//        // 2. 수동으로 만들지 말고, 조회한 엔티티 전체를 사용해 DTO를 생성합니다.
//        //    (XrayImageDto에 fromEntity static 메소드가 있다고 가정합니다.)
//        XrayImageDto xrayImageWithResult = XrayImageDto.fromEntity(savedXrayImage);
//
//        // 3. DTO에 진단 결과 정보를 추가합니다.
//        xrayImageWithResult.setDiagnosisResult(diagnosisResultDto);
//
//        // 2. 최종 PatientDto 생성 및 반환
//        PatientDto finalPatientDto = PatientDto.fromEntity(savedPatient);
//        finalPatientDto.setXrayImages(Collections.singletonList(xrayImageWithResult));
//        finalPatientDto.setDiagnosisResult(null); // 최상위 diagnosisResult는 null로 설정
//        // --- END: DTO Structure Modification ---
//
//        return finalPatientDto;
//    }
//
//    // 기존 환자 진단 시작
//    public PatientDto startDiagnosisForExistingPatient(Integer patientId, MultipartFile xrayFile) throws IOException {
//        Member member = getCurrentMember();
//        Patient patient = patientRepository.findById(patientId)
//                .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다."));
//
//        DiagnosisResultDto diagnosisResultDto = processDiagnosis(patient, member, xrayFile);
//
//        // --- START: DTO Structure Modification ---
//        // 1. 진단 결과를 포함하는 XrayImageDto 생성
//        XrayImage savedXrayImage = xrayImageRepository.findById(diagnosisResultDto.getImageId())
//                .orElseThrow(() -> new IllegalStateException("방금 저장된 X-ray 이미지를 찾을 수 없습니다. ID: " + diagnosisResultDto.getImageId()));
//
//        XrayImageDto xrayImageWithResult = XrayImageDto.fromEntity(savedXrayImage);
//        xrayImageWithResult.setDiagnosisResult(diagnosisResultDto);
//
//        // 2. 최종 PatientDto 생성 및 반환
//        PatientDto finalPatientDto = PatientDto.fromEntity(patient);
//        finalPatientDto.setXrayImages(Collections.singletonList(xrayImageWithResult));
//        finalPatientDto.setDiagnosisResult(null); // 최상위 diagnosisResult는 null로 설정
//        // --- END: DTO Structure Modification ---
//
//        return finalPatientDto;
//    }
//
//    // 공통 진단 처리 로직
//    private DiagnosisResultDto processDiagnosis(Patient patient, Member member, MultipartFile xrayFile) throws IOException {
//        // 1) 원본 업로드
//        String imageUrl = firebaseService.uploadFile(xrayFile, "xray-images");
//
//        // 2) 이미지 메타 저장
//        XrayImage xrayImage = XrayImage.builder()
//                .patient(patient)
//                .member(member)
//                .imageUrl(imageUrl)
//                .fileName(xrayFile.getOriginalFilename())
//                .fileSize((int) xrayFile.getSize())
//                .build();
//        XrayImage savedXrayImage = xrayImageRepository.save(xrayImage);
//
//        // 3) AI 서버 호출 (Mocking된 callAiServer가 호출됨)
//        AiResponseDto aiResponse = callAiServer(imageUrl);
//
//        // 4) 결과 저장 (널 방어)
//        Float prob = (aiResponse.getProbability() != null) ? aiResponse.getProbability() : 0f;
//        String camUrl  = firstNonBlank(aiResponse.getGradcamUrl());
//        if (isBlank(camUrl)) {
//            camUrl = gsToFirebaseHttps(aiResponse.getGradcamImagePath());
//        }
//        String camHttps = firstNonBlank(camUrl, "");
//
//
//        // 새로추가
//        String top1Label = null, top2Label = null, top3Label = null;
//        Float top1Prob = null, top2Prob = null, top3Prob = null;
//
//        if (aiResponse.getTop3() != null && !aiResponse.getTop3().isEmpty()) {
//            int n = aiResponse.getTop3().size();
//            if (n >= 1) {
//                top1Label = aiResponse.getTop3().get(0).getLabel();
//                top1Prob  = aiResponse.getTop3().get(0).getProbability();
//            }
//            if (n >= 2) {
//                top2Label = aiResponse.getTop3().get(1).getLabel();
//                top2Prob  = aiResponse.getTop3().get(1).getProbability();
//            }
//            if (n >= 3) {
//                top3Label = aiResponse.getTop3().get(2).getLabel();
//                top3Prob  = aiResponse.getTop3().get(2).getProbability();
//            }
//        }
//
//        if (top1Label == null) {
//            top1Label = aiResponse.getPredictedDisease();
//            top1Prob  = prob;
//        }
//
//        DiagnosisResult result = DiagnosisResult.builder()
//                .xrayImage(savedXrayImage)
//                .predictedDisease(aiResponse.getPredictedDisease())
//                .probability(prob)
//                .gradcamImagePath(camHttps)
//                .top1Disease(top1Label)
//                .top1Probability(top1Prob)
//                .top2Disease(top2Label)
//                .top2Probability(top2Prob)
//                .top3Disease(top3Label)
//                .top3Probability(top3Prob)
//                .build();
//
//        DiagnosisResult savedResult = diagnosisResultRepository.save(result);
//        return DiagnosisResultDto.fromEntity(savedResult);
//    }
//
//    private AiResponseDto callAiServer(String imageUrl) {
//        // 기존 AI 서버 호출 코드 주석 처리
//        // AiResponseDto resp = aiClient.predict(imageUrl);
//        // if (resp == null) {
//        //     throw new IllegalStateException("AI 서버 응답이 비어있습니다.");
//        // }
//        // return resp;
//
//        // 프론트엔드에 전달할 Mock 데이터 생성
//        AiResponseDto mockResponse = new AiResponseDto();
//        mockResponse.setPredictedDisease("Cardiomegaly");
//        mockResponse.setProbability(0.509504f);
//        // gradcamUrl이 아닌 gradcamImagePath로 값을 전달해야 gsToFirebaseHttps 함수에서 변환됩니다.
//        mockResponse.setGradcamImagePath("gs://eyes-of-breath.firebasestorage.app/grad-cam/20250825/acffc5f48197420192b5eb487de60ab0.jpg");
//        mockResponse.setGradcamUrl("https://firebasestorage.googleapis.com/v0/b/eyes-of-breath.firebasestorage.app/o/grad-cam%2F20250825%2Facffc5f48197420192b5eb487de60ab0.jpg?alt=media");
//
//        // Top-3 질병 정보 설정 (TopItem 클래스 사용)
//        AiResponseDto.TopItem top1 = new AiResponseDto.TopItem();
//        top1.setLabel("Cardiomegaly");
//        top1.setProbability(0.509504f);
//
//        AiResponseDto.TopItem top2 = new AiResponseDto.TopItem();
//        top2.setLabel("Support Devices");
//        top2.setProbability(0.445948f);
//
//        AiResponseDto.TopItem top3 = new AiResponseDto.TopItem();
//        top3.setLabel("Pneumonia");
//        top3.setProbability(0.415952f);
//
//        // List에 담아서 설정
//        mockResponse.setTop3(Arrays.asList(top1, top2, top3));
//        // --- 수정 끝 ---
//
//
//        return mockResponse;
//    }
//
//    @Transactional(readOnly = true)
//    public DiagnosisResultDto getDiagnosisResultById(Integer resultId) {
//        return diagnosisResultRepository.findById(resultId)
//                .map(DiagnosisResultDto::fromEntity)
//                .orElseThrow(() -> new IllegalArgumentException("진단 결과를 찾을 수 없습니다."));
//    }
//
//    public CommentDto addDoctorOpinion(Integer resultId, String content) {
//        Member member = getCurrentMember();
//        DiagnosisResult result = diagnosisResultRepository.findById(resultId)
//                .orElseThrow(() -> new IllegalArgumentException("진단 결과를 찾을 수 없습니다."));
//
//        Comment comment = Comment.builder()
//                .diagnosisResult(result)
//                .member(member)
//                .content(content)
//                .build();
//        Comment savedComment = commentRepository.save(comment);
//        return CommentDto.fromEntity(savedComment);
//    }
//
//    private Member getCurrentMember() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        return memberRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));
//    }
//
//    private static boolean isBlank(String s) {
//        return s == null || s.trim().isEmpty();
//    }
//    private static String firstNonBlank(String... options) {
//        if (options == null) return null;
//        for (String s : options) {
//            if (!isBlank(s)) return s;
//        }
//        return null;
//    }
//    private static String gsToFirebaseHttps(String gsUrl) {
//        if (isBlank(gsUrl)) return null;
//        final String prefix = "gs://";
//        if (!gsUrl.startsWith(prefix)) return gsUrl;
//        String rest = gsUrl.substring(prefix.length());
//        int slash = rest.indexOf('/');
//        if (slash < 0) return gsUrl;
//        String bucket = rest.substring(0, slash);
//        String objectPath = rest.substring(slash + 1);
//        try {
//            String encoded = java.net.URLEncoder.encode(objectPath, java.nio.charset.StandardCharsets.UTF_8.name());
//            return "https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/" + encoded + "?alt=media";
//        } catch (java.io.UnsupportedEncodingException e) {
//            // Should not happen with UTF-8
//            return gsUrl;
//        }
//    }
//}