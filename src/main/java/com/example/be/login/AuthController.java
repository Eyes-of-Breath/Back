package com.example.be.login;

import com.example.be.login.LoginRequestDto;
import com.example.be.login.FirebaseLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Firebase 로그인 API")
public class AuthController {

    private final FirebaseLoginService firebaseLoginService;

    //로그인
    @PostMapping("/login")
    @Operation(summary = "이메일/비밀번호 로그인", description = "Firebase Auth로 로그인 후 ID 토큰을 반환합니다.")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        String token = firebaseLoginService.loginWithEmailAndPassword(request.getEmail(), request.getPassword());

        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인 실패: 이메일 또는 비밀번호를 확인하세요."));
        }
    }

    //회원가입
    @PostMapping("/signup")
    @Operation(summary = "이메일/비밀번호 회원가입", description = "Firebase로 회원가입을 수행하고 ID 토큰을 반환합니다.")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto request) {
        String token = firebaseLoginService.signupWithEmailAndPassword(request.getEmail(), request.getPassword());

        if (token != null) {
            // 닉네임은 별도로 저장하고 싶다면 Firebase Firestore 연동 필요
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "회원가입 실패: 이미 존재하는 이메일일 수 있습니다."));
        }
    }
}