package com.example.be.dto.response;

import com.example.be.common.ResponseCode;
import com.example.be.common.ResponseMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor // code와 message를 받는 생성자를 만들어줍니다.
public class ResponseDto {

    // 1. 모든 응답이 공통으로 가질 필드
    private String code;
    private String message;

    // 2. 기본 성공 생성자 (이것도 필요합니다)
    public ResponseDto() {
        this.code = ResponseCode.SUCCESS;
        this.message = ResponseMessage.SUCCESS;
    }

    // 3. 데이터베이스 에러 응답 (다른 곳에서도 사용할 수 있으니 넣어두는게 좋습니다)
    public static ResponseEntity<ResponseDto> databaseError() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.DATABASE_ERROR, ResponseMessage.DATABASE_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }

    // 4. 질문 주셨던 두 개의 메서드
    // 인증 실패 응답
    public static ResponseEntity<ResponseDto> authorizationFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.AUTHORIZATION_FAIL, ResponseMessage.AUTHORIZATION_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }

    // 비밀번호 불일치 응답
    public static ResponseEntity<ResponseDto> passwordMismatch() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.PASSWORD_MISMATCH, ResponseMessage.PASSWORD_MISMATCH);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}