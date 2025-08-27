package com.example.be.dto.response;

import com.example.be.common.ResponseCode;
import com.example.be.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PasswordChangeResponseDto extends ResponseDto {

    private PasswordChangeResponseDto() {
        super(); // 부모 클래스(ResponseDto)의 성공 생성자를 호출합니다.
    }

    // 성공 응답을 생성하는 정적 메서드
    public static ResponseEntity<PasswordChangeResponseDto> success() {
        PasswordChangeResponseDto responseBody = new PasswordChangeResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}