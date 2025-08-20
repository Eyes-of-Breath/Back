package com.example.be.service;

import com.example.be.dto.response.*;
import com.example.be.dto.request.*;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<? super EmailCheckResponseDto> emailCheck(EmailCheckRequestDto emailCheckRequestDto);
    ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto emailCertificationRequestDto);
    ResponseEntity<? super CertificationCheckResponseDto> certificationCheck(CertificationCheckRequestDto certificationCheckRequestDto);
    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto signUpRequestDto);
    ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto signInRequestDto);
}
