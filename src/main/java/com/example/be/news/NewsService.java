package com.example.be.news;

import com.example.be.news.NewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    public void saveNewsBatch(List<NewsDto> newsList) {
        for (NewsDto dto : newsList) {
            // 중복 제거를 위해 URL 기준으로 존재 여부 확인
            boolean exists = newsRepository.existsByNewsUrl(dto.getNewsUrl());
            if (!exists) {
                NewsEntity entity = NewsEntity.builder()
                        .title(dto.getTitle())
                        .summary(dto.getSummary())
                        .newsUrl(dto.getNewsUrl())
                        .publishedAt(dto.getPublishedAt())
                        .crawledAt(dto.getCrawledAt())
                        .build();
                newsRepository.save(entity);
            }
        }
    }
    public List<NewsEntity> findAllNews() {
        return newsRepository.findAll(Sort.by(Sort.Direction.DESC, "crawledAt"));
    }
}
