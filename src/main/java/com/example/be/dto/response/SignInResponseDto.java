package com.example.be.dto.response;

import com.example.be.common.ResponseCode;
import com.example.be.common.ResponseMessage;
import com.example.be.dto.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SignInResponseDto extends ResponseDto {

    private String accessToken;
    private int accessTokenExpiresIn;
    private Integer memberId;
    private String nickname;

    private SignInResponseDto(String accessToken, Integer memberId, String nickname) {
        super();
        this.accessToken = accessToken;
//        this.accessTokenExpiresIn = 3600; // 1시간
        this.accessTokenExpiresIn = 3600 * 24 * 7; //7일
        this.memberId = memberId;
        this.nickname = nickname; //  닉네임 초기화
    }

    public static ResponseEntity<SignInResponseDto> success(String accessToken, Integer memberId,String nickname) { // success 메소드에 nickname 추가
        SignInResponseDto responseBody = new SignInResponseDto(accessToken, memberId, nickname);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> signInFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.SIGN_IN_FAIL, ResponseMessage.SIGN_IN_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }
}