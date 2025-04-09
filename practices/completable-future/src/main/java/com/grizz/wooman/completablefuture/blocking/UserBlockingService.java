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

// 현재 블로킹하게 작동중, 레포지토리는 1초씩
// 유저정보 조회 이후에 팔로우, 아티클, 이미지를 동시에 가져오지 않고, 순차적으로 작동하기에 3초나 걸림
// 이걸 동시성, 멀티스레딩을 이용하여 리팩토링
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
