package com.example.be.dto.response;

import com.example.be.entity.TodoItem;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class TodoItemDto {
    private Integer todoId;
    private Integer memberId;
    private String content;
    private LocalDate targetDate;
    private boolean isCompleted;

    public static TodoItemDto fromEntity(TodoItem entity) {
        return TodoItemDto.builder()
                .todoId(entity.getTodoId())
                .memberId(entity.getMember().getId())
                .content(entity.getContent())
                .targetDate(entity.getTargetDate())
                .isCompleted(entity.isCompleted())
                .build();
    }
}