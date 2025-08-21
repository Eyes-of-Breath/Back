package com.example.be.service;

import com.example.be.entity.Member;
import com.example.be.entity.Patient;
import com.example.be.entity.XrayImage;
import com.example.be.repository.MemberRepository;
import com.example.be.repository.PatientRepository;
import com.example.be.repository.XrayImageRepository;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class XrayService {

    private final XrayImageRepository xrayImageRepository;
    private final PatientRepository patientRepository;
    private final MemberRepository memberRepository;
    private final Storage storage;

    @Value("${firebase.bucket-name}")
    private String BUCKET_NAME;

    @Transactional
    public String uploadXrayImage(MultipartFile file, Integer patientId) throws IOException {
        // 1. 로그인한 사용자 정보 가져오기
        Member member = getCurrentMember();

        // 2. 환자 정보 조회
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다. ID: " + patientId));

        // 3. Firebase Storage에 업로드할 파일 이름 생성 (중복 방지)
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        String storagePath = "xray-images/" + uniqueFileName;

        // 4. Firebase Storage에 업로드
        BlobId blobId = BlobId.of(BUCKET_NAME, storagePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());

        // 5. 다운로드 URL 생성
        String encodedFileName = URLEncoder.encode(storagePath, StandardCharsets.UTF_8);
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/" + BUCKET_NAME + "/o/" + encodedFileName + "?alt=media";

        // 6. DB에 XrayImage 정보 저장
        XrayImage xrayImage = XrayImage.builder()
                .patient(patient)
                .member(member) // 사용자 정보 연결
                .imageUrl(imageUrl) // 엔티티 필드명에 맞게 'imageUrl'로 변경
                .fileName(originalFileName)
                .fileSize((int) file.getSize())
                .build();
        xrayImageRepository.save(xrayImage);

        return imageUrl;
    }

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));
    }
}