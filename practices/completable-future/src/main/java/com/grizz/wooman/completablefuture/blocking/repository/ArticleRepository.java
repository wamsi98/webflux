package com.grizz.wooman.completablefuture.blocking.repository;

import com.grizz.wooman.completablefuture.common.repository.ArticleEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ArticleRepository {
    private static List<ArticleEntity> articleEntities;

    public ArticleRepository() {
        articleEntities = List.of(
                new ArticleEntity("1", "소식1", "내용1", "1234"),
                new ArticleEntity("2", "소식2", "내용2", "1234"),
                new ArticleEntity("3", "소식3", "내용3", "10000")
        );
    }

    // userID와 맞는 게시글찾기
    // 게시글 리스트를 stream -> filter로 articleEntity.getUserId().equals와 ID비교
    // 리스트로 변환하여 리턴
    @SneakyThrows
    public List<ArticleEntity> findAllByUserId(String userId) {
        log.info("ArticleRepository.findAllByUserId: {}", userId);
        Thread.sleep(1000);
        return articleEntities.stream()
                .filter(articleEntity -> articleEntity.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
