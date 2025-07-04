package com.example.be.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

    @Schema(description = "사용자 이메일 (로그인 ID)", example = "test@example.com")
    private String email;

    @Schema(description = "비밀번호", example = "yourPassword123")
    private String password;
}