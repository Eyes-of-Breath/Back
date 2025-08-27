package com.example.be.controller;

import com.example.be.dto.request.PasswordChangeRequestDto;
import com.example.be.dto.response.PasswordChangeResponseDto;
import com.example.be.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member") // 회원 관련 API는 이 경로를 사용합니다.
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
                                             @RequestBody @Valid PasswordChangeRequestDto requestBody,
                                             @AuthenticationPrincipal String email
    ) {
        // MemberService의 changePassword 메서드를 호출하여 비즈니스 로직을 수행합니다.
        return memberService.changePassword(requestBody, email);
    }
}