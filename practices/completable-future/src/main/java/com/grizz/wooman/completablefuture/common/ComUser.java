package com.grizz.wooman.completablefuture.common;

import lombok.Data;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Data
public class ComUser {
    private final String id;
    private final String name;
    private final int age;
    private final CompletableFuture<Optional<Image>> profileImage;
    private final CompletableFuture<List<Article>> articleList;
    private final CompletableFuture<Long> followCount;
}
