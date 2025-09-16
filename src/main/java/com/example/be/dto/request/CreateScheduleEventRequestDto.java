package com.example.be.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CreateScheduleEventRequestDto {

    @NotBlank(message = "일정 제목을 입력해주세요.")
    private String title;

    @NotNull(message = "시작 시간을 입력해주세요.")
    @FutureOrPresent(message = "시작 시간은 현재 또는 미래여야 합니다.")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간을 입력해주세요.")
    @FutureOrPresent(message = "종료 시간은 현재 또는 미래여야 합니다.")
    private LocalDateTime endTime;

    private String memo;
}