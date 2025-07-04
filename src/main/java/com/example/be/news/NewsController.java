package com.example.be.news;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsRepository newsRepository;

    @PostMapping("/add")
    public News addNews(@RequestBody News news) {
        // 수집 시간 기본값 세팅
        news.setCrawledAt(LocalDateTime.now());
        return newsRepository.save(news);
    }

    @GetMapping
    public List<News> getAllNews() {
        return newsRepository.findAll();
    }
}
