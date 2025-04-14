package com.grizz.wooman.completablefuture.blocking;

import com.grizz.wooman.completablefuture.blocking.repository.ArticleRepository;
import com.grizz.wooman.completablefuture.blocking.repository.FollowRepository;
import com.grizz.wooman.completablefuture.blocking.repository.ImageRepository;
import com.grizz.wooman.completablefuture.blocking.repository.UserRepository;
import com.grizz.wooman.completablefuture.common.Article;
import com.grizz.wooman.completablefuture.common.Image;
import com.grizz.wooman.completablefuture.common.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserBlockingService {
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final ImageRepository imageRepository;
    private final FollowRepository followRepository;

    // entity 객체는 디비 개념으로 넣어놨고
    // 도메인이 객체 저장
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id)
                .map(user -> {
                    var image = imageRepository.findById(user.getProfileImageId())
                            .map(imageEntity -> {
                                return new Image(imageEntity.getId(), imageEntity.getName(), imageEntity.getUrl());
                            });

                    // entity에서 가져온 리스트들을 도메인 리스트로 변환
                    var articles = articleRepository.findAllByUserId(user.getId())
                            .stream().map(articleEntity ->
                                    new Article(articleEntity.getId(), articleEntity.getTitle(), articleEntity.getContent()))
                            .collect(Collectors.toList());

                    var followCount = followRepository.countByUserId(user.getId());

                    return new User(
                            user.getId(),
                            user.getName(),
                            user.getAge(),
                            image,
                            articles,
                            followCount
                    );
                });
    }
}
