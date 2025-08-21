package com.example.be.news;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class NewsDto {
    private String title;
    private String summary;
    private String newsUrl;
    private LocalDateTime publishedAt;
    private LocalDateTime crawledAt;
}
