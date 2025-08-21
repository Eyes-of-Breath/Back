package com.example.be.service;

import com.example.be.dto.PatientDto;
import com.example.be.entity.Member;
import com.example.be.entity.Patient;
import com.example.be.repository.MemberRepository;
import com.example.be.repository.PatientRepository;
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
}