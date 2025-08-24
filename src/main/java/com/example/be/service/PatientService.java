package com.example.be.service;

import com.example.be.dto.DiagnosisResultDto;
import com.example.be.dto.PatientDto;
import com.example.be.dto.XrayImageDto;
import com.example.be.entity.Member;
import com.example.be.entity.Patient;
import com.example.be.entity.XrayImage;
import com.example.be.repository.DiagnosisResultRepository;
import com.example.be.repository.MemberRepository;
import com.example.be.repository.PatientRepository;
import com.example.be.repository.XrayImageRepository;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // 1. 올바른 Value 어노테이션으로 수정
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // 2. 중복 import 제거

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private final DiagnosisResultRepository diagnosisResultRepository;
    private final PatientRepository patientRepository;
    private final MemberRepository memberRepository;
    private final XrayImageRepository xrayImageRepository;
    private final Storage storage;

    @Value("${firebase.bucket-name}")
    private String bucketName;

    public PatientDto createPatientWithInitialXray(PatientDto patientDto, MultipartFile xrayFile) throws IOException {
        Member member = getCurrentMember();

        if (patientRepository.existsByPatientCode(patientDto.getPatientCode())) {
            throw new IllegalArgumentException("이미 사용 중인 환자 ID입니다: " + patientDto.getPatientCode());
        }

        Patient newPatient = Patient.builder()
                .patientCode(patientDto.getPatientCode())
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

        String imageUrl = uploadFileToFirebase(xrayFile, "original/");

        XrayImage xrayImage = XrayImage.builder()
                .patient(savedPatient)
                .member(member)
                .imageUrl(imageUrl)
                .fileName(xrayFile.getOriginalFilename())
                .fileSize((int) xrayFile.getSize())
                .build();
        XrayImage savedXrayImage = xrayImageRepository.save(xrayImage);

        PatientDto resultDto = PatientDto.fromEntity(savedPatient);
        resultDto.setXrayImages(Collections.singletonList(XrayImageDto.fromEntity(savedXrayImage)));

        return resultDto;
    }

    private String uploadFileToFirebase(MultipartFile file, String folder) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String storagePath = folder + uniqueFileName;

        BlobId blobId = BlobId.of(bucketName, storagePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        String encodedFileName = URLEncoder.encode(storagePath, StandardCharsets.UTF_8);
        return "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + encodedFileName + "?alt=media";
    }

    @Transactional(readOnly = true)
    public List<PatientDto> findAllPatientsWithXrays() {
        List<Patient> allPatients = patientRepository.findAll();

        return allPatients.stream().map(patient -> {
            PatientDto patientDto = PatientDto.fromEntity(patient);

            // 1. 환자에게 속한 모든 X-ray 이미지를 찾
            List<XrayImageDto> xrayImageDtos = xrayImageRepository.findAllByPatient_PatientId(patient.getPatientId())
                    .stream()
                    .map(xrayImage -> {
                        XrayImageDto xrayImageDto = XrayImageDto.fromEntity(xrayImage);

                        // 2. 각 X-ray 이미지에 해당하는 진단 결과를 찾
                        diagnosisResultRepository.findByXrayImage(xrayImage)
                                .ifPresent(diagnosisResult -> {
                                    // 3. 진단 결과가 있으면 DTO로 변환하여 설정
                                    xrayImageDto.setDiagnosisResult(DiagnosisResultDto.fromEntity(diagnosisResult));
                                });
                        return xrayImageDto;
                    })
                    .collect(Collectors.toList());

            patientDto.setXrayImages(xrayImageDtos);
            return patientDto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientDto getPatientById(Integer patientId) {
        return patientRepository.findById(patientId)
                .map(PatientDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다. ID: " + patientId));
    }

    @Transactional(readOnly = true)
    public List<PatientDto> searchPatients(String name, LocalDate birthDate, String gender) {
        // 1. 세 조건이 모두 일치하는 환자 목록을 조회합니다.
        List<Patient> patients = patientRepository.findByNameAndBirthDateAndGender(name, birthDate, gender);

        // 2. 각 환자마다 연결된 X-ray 이미지와 진단 결과 목록을 찾아서 DTO에 담
        return patients.stream().map(patient -> {
            PatientDto patientDto = PatientDto.fromEntity(patient);

            List<XrayImageDto> xrayImageDtos = xrayImageRepository.findAllByPatient_PatientId(patient.getPatientId())
                    .stream()
                    .map(xrayImage -> {
                        XrayImageDto xrayImageDto = XrayImageDto.fromEntity(xrayImage);

                        // 각 X-ray 이미지에 해당하는 진단 결과를 찾아서 설정
                        diagnosisResultRepository.findByXrayImage(xrayImage)
                                .ifPresent(diagnosisResult ->
                                        xrayImageDto.setDiagnosisResult(DiagnosisResultDto.fromEntity(diagnosisResult))
                                );
                        return xrayImageDto;
                    })
                    .collect(Collectors.toList());

            patientDto.setXrayImages(xrayImageDtos);
            return patientDto;
        }).collect(Collectors.toList());
    }

    public PatientDto updatePatient(Integer patientId, PatientDto patientDto) {
        Member member = getCurrentMember();
        Patient patient = patientRepository.findByPatientIdAndMember_Id(patientId, member.getId())
                .orElseThrow(() -> new SecurityException("수정 권한이 없거나 존재하지 않는 환자입니다."));
        patient.update(patientDto);
        return PatientDto.fromEntity(patient);
    }

    public void deletePatient(Integer patientId) {
        Member member = getCurrentMember();
        if (!patientRepository.existsById(patientId)) {
            throw new IllegalArgumentException("존재하지 않는 환자 ID입니다.");
        }
        Patient patient = patientRepository.findByPatientIdAndMember_Id(patientId, member.getId())
                .orElseThrow(() -> new SecurityException("삭제 권한이 없는 환자입니다."));
        patientRepository.delete(patient);
    }

    public void deleteAllPatientsByCurrentUser() {
        Member member = getCurrentMember();
        patientRepository.deleteAllByMemberId(member.getId());
    }

    // 3. 누락되었던 getCurrentMember() 메소드 추가
    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<PatientDto> findAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(PatientDto::fromEntity)
                .collect(Collectors.toList());
    }
}
