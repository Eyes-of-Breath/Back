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
        try {
            // 테스트용 회원
            Member member = memberRepository.findById(1L)
                    .orElseThrow(() -> new IllegalArgumentException("테스트용 회원을 찾을 수 없습니다."));

            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다. ID: " + patientId));

            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            String storagePath = "original/" + uniqueFileName;

            BlobId blobId = BlobId.of(BUCKET_NAME, storagePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            String encodedFileName = URLEncoder.encode(storagePath, StandardCharsets.UTF_8);
            String imageUrl = "https://firebasestorage.googleapis.com/v0/b/" + BUCKET_NAME + "/o/" + encodedFileName + "?alt=media";

            XrayImage xrayImage = XrayImage.builder()
                    .patient(patient)
                    .member(member)
                    .imageUrl(imageUrl)
                    .fileName(originalFileName)
                    .fileSize((int) file.getSize())
                    .build();

            xrayImageRepository.save(xrayImage);
            return imageUrl;

        } catch (Exception e) {
            System.out.println("업로드 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 👇 이 함수도 클래스 내부에 들어가야 합니다
    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));
    }
}
