package com.example.be.controller;

import com.example.be.dto.PatientDto;
import com.example.be.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // 1. 신규 환자 등록 API
    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@RequestBody PatientDto patientDto) {
        PatientDto createdPatient = patientService.createPatient(patientDto);
        return ResponseEntity.ok(createdPatient);
    }

    // 2. 환자 동적 검색 API
    @GetMapping("/search")
    public ResponseEntity<List<PatientDto>> searchPatients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam(required = false) String gender
    ) {
        List<PatientDto> patients = patientService.searchPatients(name, birthDate, gender);
        return ResponseEntity.ok(patients);
    }

    // 3. 환자 정보 조회 API (DB ID 기준)
    @GetMapping("/{patientId}")
    public ResponseEntity<PatientDto> getPatient(@PathVariable Integer patientId) { // 👈 Long을 Integer로 수정
        PatientDto patientDto = patientService.getPatientById(patientId);
        return ResponseEntity.ok(patientDto);
    }

    // 4. 환자 정보 수정 API
    @PutMapping("/{patientId}")
    public ResponseEntity<PatientDto> updatePatient(
            @PathVariable Integer patientId,
            @RequestBody PatientDto patientDto) {
        PatientDto updatedPatient = patientService.updatePatient(patientId, patientDto);
        return ResponseEntity.ok(updatedPatient);
    }

    // 5. 특정 환자 삭제 API
    @DeleteMapping("/{patientId}")
    public ResponseEntity<Map<String, String>> deletePatient(@PathVariable Integer patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.ok(Map.of("message", "환자 정보가 성공적으로 삭제되었습니다."));
    }

    // 6. 현재 사용자의 모든 환자 기록 삭제 API
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAllMyPatients() {
        patientService.deleteAllPatientsByCurrentUser();
        return ResponseEntity.ok(Map.of("message", "모든 환자 정보가 성공적으로 삭제되었습니다."));
    }
}