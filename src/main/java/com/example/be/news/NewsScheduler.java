package com.example.be.news;

import com.example.be.news.NewsDto;
import com.example.be.news.KormediCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsScheduler {

    private final NewsService newsService;
    // 매 1시간마다 실행
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void crawlAndSaveNews() {
        log.info("[뉴스 스케줄러] 코메디 사이트에서 뉴스 크롤링 시작");
        List<NewsDto> newsList = KormediCrawler.crawlFromRss();
        log.info("크롤링된 뉴스 개수: {}", newsList.size());
        newsService.saveNewsBatch(newsList);
    }
}
