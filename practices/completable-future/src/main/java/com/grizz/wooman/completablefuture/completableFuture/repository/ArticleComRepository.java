package com.grizz.wooman.completablefuture.completableFuture.repository;

import com.grizz.wooman.completablefuture.common.repository.ArticleEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class ArticleComRepository {
    private static List<ArticleEntity> articleEntities;

    public ArticleComRepository() {
        articleEntities = List.of(
                new ArticleEntity("1", "소식1", "내용1", "1234"),
                new ArticleEntity("2", "소식2", "내용2", "1234"),
                new ArticleEntity("3", "소식3", "내용3", "10000")
        );
    }

    // userID와 맞는 게시글찾기
    // 게시글 리스트를 stream -> filter로 articleEntity.getUserId().equals와 ID비교
    // 리스트로 변환하여 리턴

    /**
     * Thread.sleep()을 future 구현 안에 위치한 이유
     * supplyAsync() 실행 구문 위에 sleep이 존재하면, findAllByUserId을 호출하는 caller가 1초간 강제로 block이 된다.
     * 그렇기 때문에 findAllByUserId호출 후 supplyAsync()라는 비동기 작업 내에서 실행하면
     * supplyAsync()는 스레드 풀에서 작동하기 때문에, 1초 sleep은 다른 스레드 풀에서 작동하게 된다
     * @param userId
     * @return
     */
    @SneakyThrows
    public CompletableFuture<List<ArticleEntity>> findAllByUserId(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("ArticleRepository.findAllByUserId: {}", userId);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return articleEntities.stream()
                    .filter(articleEntities -> articleEntities.getUserId().equals(userId))
                    .collect(Collectors.toList());
        });
    }
}
