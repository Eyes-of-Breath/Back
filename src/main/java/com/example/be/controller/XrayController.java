//package com.example.be.controller;
//
//import com.example.be.service.XrayService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import java.util.Map;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/xray")
//public class XrayController {
//
//    private final XrayService xrayService;
//
//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadXrayImage(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("patientId") Integer patientId) {
//        try {
//            String imageUrl = xrayService.uploadXrayImage(file, patientId);
//            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("업로드 실패: " + e.getMessage());
//        }
//    }
//}