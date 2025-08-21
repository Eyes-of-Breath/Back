package com.example.be.news;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KormediCrawler {

    private static final String RSS_URL = "https://kormedi.com/rss";
    private static final Set<String> KEYWORDS = Set.of("의료", "바이오", "의사", "병원", "병", "암", "수술", "AI", "인공지능");

    public static List<NewsDto> crawlFromRss() {
        List<NewsDto> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(RSS_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements items = doc.select("item");
            for (Element item : items) {
                String title = item.selectFirst("title").text();
                String link = item.selectFirst("link").text();
                String pubDate = item.selectFirst("pubDate").text();

                // 키워드 필터
                if (KEYWORDS.stream().noneMatch(title::contains)) {
                    continue;
                }

                String summary = extractSummaryFromArticle(link);

                NewsDto dto = NewsDto.builder()
                        .title(title.length() > 255 ? title.substring(0, 255) : title)
                        .summary(summary)
                        .newsUrl(link)
                        .publishedAt(parsePubDate(pubDate))
                        .crawledAt(LocalDateTime.now())
                        .build();

                newsList.add(dto);
            }
        } catch (Exception e) {
            log.error("[KormediCrawler] 크롤링 실패: ", e);
        }
        return newsList.stream()
                .filter(n -> n.getNewsUrl() != null && !n.getNewsUrl().isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private static LocalDateTime parsePubDate(String raw) {
        try {
            return LocalDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractSummaryFromArticle(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // 1. <meta name="description">
            Element metaDesc = doc.selectFirst("meta[name=description]");
            if (metaDesc != null && metaDesc.hasAttr("content")) {
                return metaDesc.attr("content").strip();
            }

            // 2. 본문에서 첫 번째 <p> 추출
            Element p = doc.selectFirst(".entry-content p, .post-content p, article p");
            if (p != null) {
                String text = p.text().strip();
                return text.length() > 220 ? text.substring(0, 220) + "…" : text;
            }
        } catch (Exception e) {
            log.warn("[KormediCrawler] 요약 추출 실패 ({}): {}", url, e.getMessage());
        }
        return "";
    }
}
