package com.example.be.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {

    @Schema(description = "사용자 이메일", example = "test@example.com")
    private String email;

    @Schema(description = "비밀번호", example = "yourPassword123")
    private String password;

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;
}
