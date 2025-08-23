package com.example.be.dto;

import com.example.be.entity.Comment;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class CommentDto {
    private Integer commentId;
    private Integer resultId;
    private Integer memberId; // 작성자 ID
    private String memberNickname; // 작성자 닉네임
    private String content;
    private LocalDateTime createdAt;
    private Integer patientId;

    public static CommentDto fromEntity(Comment entity) {
        return CommentDto.builder()
                .commentId(entity.getCommentId())
                .resultId(entity.getDiagnosisResult().getResultId())
                .memberId(entity.getMember().getId())
                .memberNickname(entity.getMember().getNickname())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .patientId(entity.getDiagnosisResult().getXrayImage().getPatient().getPatientId())
                .build();
    }
}