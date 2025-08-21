package com.example.be.news;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsRepository newsRepository;

    @PostMapping("/add")
    public NewsEntity addNews(@RequestBody NewsEntity news) {
        // 수집 시간 기본값 세팅
        news.setCrawledAt(LocalDateTime.now());
        return newsRepository.save(news);
    }

    @GetMapping
    public List<NewsEntity> getAllNews() {
        return newsRepository.findAll(Sort.by(Sort.Direction.DESC, "crawledAt"));
    }
}
