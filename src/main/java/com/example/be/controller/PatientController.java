package com.example.be.controller;

import com.example.be.dto.PatientDto;
import com.example.be.service.PatientService;
import jakarta.validation.Valid; // Valid import 추가
import jakarta.validation.constraints.Pattern; // Pattern import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Validated // 클래스 레벨에 @Validated 추가 (RequestParam 유효성 검사를 위해)
public class PatientController {

    private final PatientService patientService;

//    // 1. 신규 환자 등록 API
//    @PostMapping(value = "/with-initial-xray", consumes = "multipart/form-data")
//    public ResponseEntity<PatientDto> createPatientWithInitialXray(
//            @RequestPart("file") MultipartFile file,
//            @RequestParam("patientCode") String patientCode,
//            @RequestParam("name") String name,
//            @RequestParam("birthDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
//            @RequestParam(value = "gender", required = false) @Pattern(regexp = "^[MF]$", message = "성별은 'M' 또는 'F' 값만 가능합니다.") String gender,
//            @RequestParam(value = "bloodType", required = false) String bloodType,
//            @RequestParam(value = "height", required = false) Float height,
//            @RequestParam(value = "weight", required = false) Float weight,
//            @RequestParam(value = "country", required = false) String country
//    ) throws IOException {
//
//        // 파라미터로 받은 환자 정보를 DTO 객체로 만듭니다.
//        PatientDto patientDto = PatientDto.builder()
//                .patientCode(patientCode)
//                .name(name)
//                .birthDate(birthDate)
//                .gender(gender)
//                .bloodType(bloodType)
//                .height(height)
//                .weight(weight)
//                .country(country)
//                .build();
//
//        PatientDto createdPatient = patientService.createPatientWithInitialXray(patientDto, file);
//        return ResponseEntity.ok(createdPatient);
//    }

//    // 1. 신규 환자 등록 API
//    @PostMapping
//    public ResponseEntity<PatientDto> createPatient(@RequestBody PatientDto patientDto) {
//        PatientDto createdPatient = patientService.createPatient(patientDto);
//        return ResponseEntity.ok(createdPatient);
//    }

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
            // @RequestBody 앞에 @Valid를 추가하여 DTO 유효성 검사 활성화
            @Valid @RequestBody PatientDto patientDto) {
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

    // 7. 모든 환자 목록 조회 API
    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        List<PatientDto> allPatients = patientService.findAllPatients();
        return ResponseEntity.ok(allPatients);
    }

    // 8. 모든 환자 및 X-ray 이미지 목록 조회 API (새로 추가)
    @GetMapping("/with-xrays")
    public ResponseEntity<List<PatientDto>> getAllPatientsWithXrays() {
        List<PatientDto> allPatientsWithXrays = patientService.findAllPatientsWithXrays();
        return ResponseEntity.ok(allPatientsWithXrays);
    }
}