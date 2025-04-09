package com.grizz.wooman.completablefuture.completableFuture.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class FollowComRepository {
    private Map<String, Long> userFollowCountMap;

    public FollowComRepository() {
        userFollowCountMap = Map.of("1234", 1000L);
    }

    // 1234면 1000명의 팔로우
    // 아니면 0이라는 디폴트
    @SneakyThrows
    public CompletableFuture<Long> countByUserId(String userId) {
        return CompletableFuture.supplyAsync(()->{
            log.info("FollowRepository.countByUserId: {}", userId);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return userFollowCountMap.getOrDefault(userId, 0L);
        });
    }
}

