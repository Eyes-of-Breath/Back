package com.example.be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 간단한 요청 DTO이므로 생성자만 추가
public class AiRequestDto {
    private String imageUrl;
}