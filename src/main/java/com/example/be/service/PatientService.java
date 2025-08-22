package com.example.be.service;

import com.example.be.dto.PatientDto;
import com.example.be.dto.XrayImageDto;
import com.example.be.entity.Member;
import com.example.be.entity.Patient;
import com.example.be.repository.MemberRepository;
import com.example.be.repository.PatientRepository;
import com.example.be.repository.XrayImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final MemberRepository memberRepository; // Member를 조회하기 위해 주입
    private final XrayImageRepository xrayImageRepository;

    // 환자 생성
    public PatientDto createPatient(PatientDto patientDto) {
        // 1. 현재 로그인한 사용자의 이메일 가져오기
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. 이메일을 사용해 Member 엔티티 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));

        // 환자 고유 ID 중복 검사
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
                .member(member) // 3. 조회한 Member 엔티티를 Patient에 연결
                .build();

        Patient savedPatient = patientRepository.save(newPatient);
        return PatientDto.fromEntity(savedPatient);
    }

    //모든 환자 정보와 각 환자의 X-ray 이미지 목록을 함께 조회하는 메소드 (새로 추가)
    @Transactional(readOnly = true)
    public List<PatientDto> findAllPatientsWithXrays() {
        // 1. 모든 환자 정보를 조회합니다.
        List<Patient> allPatients = patientRepository.findAll();

        // 2. 각 환자(Patient)를 PatientDto로 변환합니다.
        return allPatients.stream().map(patient -> {
            // 3. Patient 엔티티를 PatientDto로 변환합니다.
            PatientDto patientDto = PatientDto.fromEntity(patient);

            // 4. 해당 환자의 ID로 모든 X-ray 이미지들을 조회합니다.
            List<XrayImageDto> xrayImageDtos = xrayImageRepository.findAllByPatient_PatientId(patient.getPatientId())
                    .stream()
                    .map(XrayImageDto::fromEntity) // XrayImage를 XrayImageDto로 변환
                    .collect(Collectors.toList());

            // 5. 조회된 이미지 DTO 리스트를 환자 DTO에 설정합니다.
            patientDto.setXrayImages(xrayImageDtos);

            return patientDto;
        }).collect(Collectors.toList());
    }

    // 환자 정보 조회 (DB ID 기준)
    @Transactional(readOnly = true)
    public PatientDto getPatientById(Integer patientId) {
        return patientRepository.findById(patientId)
                .map(PatientDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("환자를 찾을 수 없습니다. ID: " + patientId));
    }

    // 환자 동적 검색
    @Transactional(readOnly = true)
    public List<PatientDto> searchPatients(String name, LocalDate birthDate, String gender) {
        if (name == null && birthDate == null && gender == null) {
            return List.of();
        }
        List<Patient> patients = patientRepository.findByCriteria(name, birthDate, gender);
        return patients.stream()
                .map(PatientDto::fromEntity)
                .collect(Collectors.toList());
    }
    // 환자 정보 수정 (소유권 확인)
    public PatientDto updatePatient(Integer patientId, PatientDto patientDto) {
        Member member = getCurrentMember();
        Patient patient = patientRepository.findByPatientIdAndMember_Id(patientId, member.getId())
                .orElseThrow(() -> new SecurityException("수정 권한이 없거나 존재하지 않는 환자입니다."));

        patient.update(patientDto);
        // patientRepository.save(patient)를 호출할 필요가 없습니다.
        // @Transactional 안에서 엔티티를 변경하면 자동으로 DB에 반영됩니다 (더티 체킹).
        return PatientDto.fromEntity(patient);
    }

    // 특정 환자 삭제 (소유권 확인)
    public void deletePatient(Integer patientId) {
        Member member = getCurrentMember();
        if (!patientRepository.existsById(patientId)) {
            throw new IllegalArgumentException("존재하지 않는 환자 ID입니다.");
        }
        Patient patient = patientRepository.findByPatientIdAndMember_Id(patientId, member.getId())
                .orElseThrow(() -> new SecurityException("삭제 권한이 없는 환자입니다."));

        patientRepository.delete(patient);
    }

    // 로그인한 사용자의 모든 환자 기록 삭제
    public void deleteAllPatientsByCurrentUser() {
        Member member = getCurrentMember();
        patientRepository.deleteAllByMemberId(member.getId());
    }

    // [Helper Method] 현재 로그인한 사용자(Member) 정보 가져오기
    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<PatientDto> findAllPatients() {
        return patientRepository.findAll() // 모든 환자 데이터를 가져옴
                .stream()                  // 스트림으로 변환
                .map(PatientDto::fromEntity) // 각 환자 엔티티를 DTO로 변환
                .collect(Collectors.toList()); // 리스트로 만듦
    }
}