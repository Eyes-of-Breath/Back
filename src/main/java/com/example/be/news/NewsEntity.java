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
public class NewsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer newsId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String newsUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private LocalDateTime publishedAt;
    private LocalDateTime crawledAt;
}
