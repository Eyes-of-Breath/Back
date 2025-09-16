package com.example.be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class CreateTodoItemRequestDto {

    @NotBlank(message = "할 일 내용을 입력해주세요.")
    private String content;

    @NotNull(message = "날짜를 지정해주세요.")
    private LocalDate targetDate;
}