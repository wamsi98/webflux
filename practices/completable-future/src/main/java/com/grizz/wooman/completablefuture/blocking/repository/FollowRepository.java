package com.grizz.wooman.completablefuture.blocking.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FollowRepository {
    private Map<String, Long> userFollowCountMap;

    public FollowRepository() {
        userFollowCountMap = Map.of("1234", 1000L);
    }

    // 1234면 1000명의 팔로우
    // 아니면 0이라는 디폴트
    @SneakyThrows
    public Long countByUserId(String userId) {
        log.info("FollowRepository.countByUserId: {}", userId);
        Thread.sleep(1000);
        return userFollowCountMap.getOrDefault(userId, 0L);
    }
}

