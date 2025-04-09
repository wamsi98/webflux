package com.grizz.wooman.completablefuture.completableFuture.repository;

import com.grizz.wooman.completablefuture.common.repository.UserEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class UserComRepository {
    private final Map<String, UserEntity> userMap;

    public UserComRepository() {
        var user = new UserEntity("1234", "taewoo", 32, "image#1000");

        userMap = Map.of("1234", user); // 유저정보 map
    }

    // 1초 sleep 후,맵에서 ID를 찾고, optional로 값을 리턴하는 
    // 현재 1234 아이디 아니면 못찾음
    @SneakyThrows
    public CompletableFuture<Optional<UserEntity>> findById(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("UserRepository.findById: {}", userId);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            var user = userMap.get(userId);
           return Optional.ofNullable(user);
        });
    }
}
