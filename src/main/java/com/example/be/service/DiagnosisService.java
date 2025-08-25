package com.example.be.service;

import com.example.be.dto.CommentDto;
import com.example.be.dto.DiagnosisResultDto;
import com.example.be.dto.PatientDto;
import com.example.be.dto.XrayImageDto;
import com.example.be.dto.response.AiResponseDto;
import com.example.be.entity.*;
import com.example.be.repository.*;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final CommentRepository commentRepository;

//    @Value("${firebase.bucket-name}")
//    private String bucketName; // application.yml에서 버킷 이름 주입

    // 신규 환자 진단 시작
    public PatientDto startDiagnosisForNewPatient(PatientDto patientDto, MultipartFile xrayFile) throws IOException { //반환 타입을 PatientDto로 변경
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

        // 공통 진단 로직을 호출하고, 최종적으로 환자 정보까지 담아서 반환
        DiagnosisResultDto diagnosisResultDto = processDiagnosis(savedPatient, member, xrayFile);

        // 최종적으로 프론트엔드에 전달할 데이터 조합
        PatientDto finalPatientDto = PatientDto.fromEntity(savedPatient);
        finalPatientDto.setDiagnosisResult(diagnosisResultDto);
        finalPatientDto.setXrayImages(Collections.singletonList(
                XrayImageDto.builder()
                        .imageId(diagnosisResultDto.getImageId())
                        .imageUrl(diagnosisResultDto.getImageUrl())
                        .build()
        ));

        return finalPatientDto; // 모든 정보가 담긴 PatientDto 반환
    }

    // 기존 환자 진단 시작
    public PatientDto startDiagnosisForExistingPatient(Integer patientId, MultipartFile xrayFile) throws IOException { // 반환 타입을 PatientDto로 변경
        Member member = getCurrentMember();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다."));

        // 공통 진단 로직 호출
        DiagnosisResultDto diagnosisResultDto = processDiagnosis(patient, member, xrayFile);

        // 최종적으로 프론트엔드에 전달할 데이터 조합
        PatientDto finalPatientDto = PatientDto.fromEntity(patient); // 기존 환자 정보 사용
        finalPatientDto.setDiagnosisResult(diagnosisResultDto);
        // 기존 환자의 X-ray 목록을 모두 불러와서 추가해 줄 수도 있고,
        // 일단은 방금 찍은 X-ray 정보만 포함해서 보낼 수도 있습니다. 여기서는 후자를 택합니다.
        finalPatientDto.setXrayImages(Collections.singletonList(
                XrayImageDto.builder()
                        .imageId(diagnosisResultDto.getImageId())
                        .imageUrl(diagnosisResultDto.getImageUrl())
                        .build()
        ));

        return finalPatientDto; // 모든 정보가 담긴 PatientDto 반환
    }

    // 공통 진단 처리 로직
    private DiagnosisResultDto processDiagnosis(Patient patient, Member member, MultipartFile xrayFile) throws IOException {
        String imageUrl = firebaseService.uploadFile(xrayFile, "xray-images");

        XrayImage xrayImage = XrayImage.builder()
                .patient(patient)
                .member(member)
                .imageUrl(imageUrl)
                .fileName(xrayFile.getOriginalFilename())
                .fileSize((int) xrayFile.getSize())
                .build();
        XrayImage savedXrayImage = xrayImageRepository.save(xrayImage);

        AiResponseDto aiResponse = callAiServer(imageUrl);

        DiagnosisResult result = DiagnosisResult.builder()
                .xrayImage(savedXrayImage)
                .predictedDisease(aiResponse.getPredictedDisease())
                .probability(aiResponse.getProbability())
                .gradcamImagePath(aiResponse.getGradcamUrl())
                .build();

        DiagnosisResult savedResult = diagnosisResultRepository.save(result);
        return DiagnosisResultDto.fromEntity(savedResult);
    }

    private AiResponseDto callAiServer(String imageUrl) {
        // 실제 AI 서버가 완성되면 이 부분을 실제 API 호출 코드로 변경
        AiResponseDto mockResponse = new AiResponseDto();
        mockResponse.setPredictedDisease("폐렴 (Pneumonia)");
        mockResponse.setProbability(0.925f);
        mockResponse.setGradcamUrl("https://firebasestorage.googleapis.com/v0/b/your-bucket/gradcam/dummy_gradcam.jpg");
        return mockResponse;
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
//    /**
//     * AI 모델을 호출하여 Grad-CAM 이미지를 생성하는 메소드 (시뮬레이션)
//     * @param xrayFile 원본 X-ray 이미지 파일
//     * @return 생성된 Grad-CAM 이미지의 byte 배열
//     */
//    private byte[] getGradCamImageFromAIModel(MultipartFile xrayFile) {
//        // TODO: 여기에 실제 AI 모델 API를 호출하고, 결과로 받은 이미지 데이터를 byte[] 형태로 반환하는 로직을 구현해야 합니다.
//        // 현재는 시뮬레이션을 위해 비어있는 byte 배열을 반환합니다.
//        System.out.println("AI 모델 호출 시뮬레이션: " + xrayFile.getOriginalFilename());
//        return new byte[0];
//    }

//    /**
//     * MultipartFile을 Firebase Storage에 업로드하는 헬퍼 메소드
//     * @param file 업로드할 파일
//     * @param folder 저장할 폴더 경로 (e.g., "original/")
//     * @return 업로드된 파일의 URL
//     */
//    private String uploadFileToFirebase(MultipartFile file, String folder) throws IOException {
//        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//        String storagePath = folder + uniqueFileName;
//
//        BlobId blobId = BlobId.of(bucketName, storagePath);
//        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
//                .setContentType(file.getContentType())
//                .build();
//
//        storage.create(blobInfo, file.getBytes());
//
//        String encodedFileName = URLEncoder.encode(storagePath, StandardCharsets.UTF_8);
//        return "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + encodedFileName + "?alt=media";
//    }
//
//    /**
//     * byte 배열을 Firebase Storage에 업로드하는 헬퍼 메소드
//     * @param fileBytes 업로드할 파일의 byte 배열
//     * @param folder 저장할 폴더 경로 (e.g., "grad-cam/")
//     * @param originalFileName 원본 파일 이름 (고유 파일명 생성에 사용)
//     * @return 업로드된 파일의 URL
//     */
//    private String uploadBytesToFirebase(byte[] fileBytes, String folder, String originalFileName) throws IOException {
//        String uniqueFileName = UUID.randomUUID().toString() + "_gradcam_" + originalFileName;
//        String storagePath = folder + uniqueFileName;
//
//        BlobId blobId = BlobId.of(bucketName, storagePath);
//        // Grad-CAM 이미지는 보통 jpeg 또는 png 형식이므로 Mime Type을 고정하거나 파라미터로 받을 수 있습니다.
//        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
//                .setContentType("image/jpeg")
//                .build();
//
//        storage.create(blobInfo, fileBytes);
//
//        String encodedFileName = URLEncoder.encode(storagePath, StandardCharsets.UTF_8);
//        return "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + encodedFileName + "?alt=media";
//    }
//
//    @Transactional
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
//
//        Comment savedComment = commentRepository.save(comment);
//
//        return CommentDto.fromEntity(savedComment);
//    }
//
//    private Member getCurrentMember() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        return memberRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));
//    }
//
//    @Transactional(readOnly = true)
//    public DiagnosisResultDto getDiagnosisResultById(Integer resultId) {
//        return diagnosisResultRepository.findById(resultId)
//                .map(DiagnosisResultDto::fromEntity)
//                .orElseThrow(() -> new IllegalArgumentException("진단 결과를 찾을 수 없습니다."));
//    }
}
