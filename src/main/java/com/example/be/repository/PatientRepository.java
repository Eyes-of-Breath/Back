package com.example.be.repository;

import com.example.be.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {

    // 특정 사용자가 등록한 모든 환자 정보 삭제
    @Modifying // 이 어노테이션은 INSERT, UPDATE, DELETE 쿼리에 필요합니다.
    @Query("DELETE FROM Patient p WHERE p.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Integer memberId);

    // memberId와 patientId로 환자 정보를 찾는 메소드 (소유권 확인용)
    Optional<Patient> findByPatientIdAndMember_Id(Integer patientId, Integer memberId);

    // 환자 코드(고유 ID)로 환자가 존재하는지 확인
    boolean existsByPatientCode(String patientCode);

    // patientCode로 환자를 찾는 메소드 추가
    Optional<Patient> findByPatientCode(String patientCode);

    // 검색 조건이 모두 일치하는 환자를 찾는 메소드
    // 이름, 생년월일, 성별로 동적 검색
    @Query("SELECT p FROM Patient p WHERE " +
            "(:name IS NULL OR p.name LIKE %:name%) AND " +
            "(:birthDate IS NULL OR p.birthDate = :birthDate) AND " +
            "(:gender IS NULL OR p.gender = :gender)")
//    List<Patient> findByCriteria(
//            @Param("name") String name,
//            @Param("birthDate") LocalDate birthDate,
//            @Param("gender") String gender
//    );
    List<Patient> findByNameAndBirthDateAndGender(String name, LocalDate birthDate, String gender);



}