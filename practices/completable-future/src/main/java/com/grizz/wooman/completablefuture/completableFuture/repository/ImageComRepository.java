package com.grizz.wooman.completablefuture.completableFuture.repository;

import com.grizz.wooman.completablefuture.common.repository.ImageEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ImageComRepository {
    private final Map<String, ImageEntity> imageMap;

    public ImageComRepository() {
        imageMap = Map.of(
                "image#1000", new ImageEntity("image#1000", "profileImage", "https://dailyone.com/images/1000")
        );
    }

    // 이미지 map에서 ID로 get했을 때 있으면, value인 ImageEntity를 리턴
    // 없으면 optional empty
    @SneakyThrows
    public CompletableFuture<Optional<ImageEntity>> findById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("ImageRepository.findById: {}", id);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return Optional.ofNullable(imageMap.get(id));
        });
    }
}
