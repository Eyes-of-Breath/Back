package com.example.be.news;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/news")
public class NewsController {

//    private final NewsRepository newsRepository;
//
//    @PostMapping("/add")
//    public NewsEntity addNews(@RequestBody NewsEntity news) {
//        // 수집 시간 기본값 세팅
//        news.setCrawledAt(LocalDateTime.now());
//        return newsRepository.save(news);
//    }
//
//    @GetMapping
//    public List<NewsEntity> getAllNews() {
//        return newsRepository.findAll(Sort.by(Sort.Direction.DESC, "crawledAt"));
//    }
// Repository 대신 Service를 주입받도록 변경
    private final NewsService newsService;

    // 테스트용으로 남겨두거나, 필요 없다면 삭제해도 됩니다.
    @PostMapping("/add")
    public NewsEntity addNews(@RequestBody NewsDto dto) {
        // 이 부분은 서비스 계층으로 로직을 옮기는 것이 좋습니다.
        // 지금은 간단히 예시로만 남겨둡니다.
        return null; // 실제 구현 필요
    }

    // getAllNews 메서드가 Service를 호출하도록 변경
    @GetMapping
    public List<NewsEntity> getAllNews() {
        return newsService.findAllNews();
    }

}
