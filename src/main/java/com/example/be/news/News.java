package com.example.be.news;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer newsId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String newsUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private LocalDateTime publishedAt;

    private LocalDateTime crawledAt;
}
