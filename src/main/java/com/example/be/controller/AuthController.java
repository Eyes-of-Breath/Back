package com.example.be.controller;

import com.example.be.dto.request.*;
import com.example.be.dto.response.*;
import com.example.be.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/check-email")
    public ResponseEntity<? super EmailCheckResponseDto> emailCheck(
            @RequestBody @Valid EmailCheckRequestDto requestBody
    ) {
        return authService.emailCheck(requestBody);
    }

    @PostMapping("/send-certification-email")
    public ResponseEntity<? super EmailCertificationResponseDto> sendCertificationEmail(
            @RequestBody @Valid EmailCertificationRequestDto requestBody
    ) {
        return authService.emailCertification(requestBody);
    }

    @PostMapping("/check-certification-number")
    public ResponseEntity<? super CertificationCheckResponseDto> checkCertificationNumber(
            @RequestBody @Valid CertificationCheckRequestDto requestBody
    ) {
        return authService.certificationCheck(requestBody);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<? super SignUpResponseDto> signUp(
            @RequestBody @Valid SignUpRequestDto requestBody
    ) {
        return authService.signUp(requestBody);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<? super SignInResponseDto> signIn(
            @RequestBody @Valid SignInRequestDto requestBody
    ) {
        return authService.signIn(requestBody);
    }
}