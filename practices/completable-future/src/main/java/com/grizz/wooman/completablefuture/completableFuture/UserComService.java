package com.grizz.wooman.completablefuture.completableFuture;

import com.grizz.wooman.completablefuture.common.Article;
import com.grizz.wooman.completablefuture.common.Image;
import com.grizz.wooman.completablefuture.common.User;
import com.grizz.wooman.completablefuture.common.repository.UserEntity;
import com.grizz.wooman.completablefuture.completableFuture.repository.ArticleComRepository;
import com.grizz.wooman.completablefuture.completableFuture.repository.FollowComRepository;
import com.grizz.wooman.completablefuture.completableFuture.repository.ImageComRepository;
import com.grizz.wooman.completablefuture.completableFuture.repository.UserComRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UserComService {
    private final UserComRepository userComRepository;
    private final ArticleComRepository articleComRepository;
    private final ImageComRepository imageComRepository;
    private final FollowComRepository followComRepository;

    // 가능한한 각 레포지토리를 future로 반환

    /**
     * method reference로 한번 더 간소화 가능
     *
     * return userComRepository.findById(id).get() 으로하면 block임. get()이 강제로 대기하기 때문
     *
     * findById는 Optional<UserEntity> 를 반환중인데, Optional<User> 가 반횐되도록
     *
     * 0. "임시" thenApply는 T를 받아 U로 값을 가공하는데, 우선 findById의 리턴값인 UserEntity를 User로 바꾸는 과정이 추가되어야 할듯?
     * 1. getUserById에서 get()이 문제를 일으키고 있었으므로, 여기에 비동기 처리를 위해  CompletableFuture 추가 + get() 삭제
     * 2. userComRepository.findById는 Optional<UserEntity>를 반환하므로, 통째로 넘긴다
     * 3. 각각 동시에 다른 스레드에서 실행됨
     * @param id
     * @return
     */
    @SneakyThrows
    public CompletableFuture<Optional<User>> getUserById(String id) {            // thenApply를 사용하면, CompletableFuture<CompletableFuture<Optional<User>>>가 되는데 , 이걸 thenCompose로 해결
        return userComRepository.findById(id)         // 문제의 get()지점? , get()에서 future를 다 대기했던 문제 해결
                .thenComposeAsync(this::getUser);
//                .map(userEntity -> {
//                    return getUser(userEntity);
//                });
    }


    /**
     * 0. 각 레포지토리에 있는 get들도 걷어내야 하고, 리턴타입도 future로 변경해야함
     * 1. get()이 있으면 동기 blocking상태이다.. thenApplyAsync 를 추가하여 비동기적 처리?
     * @param userEntityOptional
     * @return
     */
    @SneakyThrows
    private CompletableFuture<Optional<User>> getUser(Optional<UserEntity> userEntityOptional) {                   // 2.1 타입 변경 Optional<UserEntity>
        if (userEntityOptional.isEmpty()) return CompletableFuture.completedFuture(Optional.empty());                          // 비어있다면 굳이 더 진행할 필요가 없기 떄문에
//        if (userEntityOptional.isEmpty()) {
//            CompletableFuture<Optional<User>> future = new CompletableFuture<Optional<User>>();
//            future.complete(Optional.empty());
//            return future;                          // 비어있다면 굳이 더 진행할 필요가 없기 떄문에
//        }
        UserEntity userEntity = userEntityOptional.get();

        // imageComRepository.findById 리턴타입 = CompletableFuture<Optional<ImageEntity>>
        // imageEntity를 image로 변환해서 리턴해주려면, thenApply를 활용해볼 수 있을 것
        CompletableFuture<Optional<Image>> imageFuture = imageComRepository.findById(userEntity.getProfileImageId())
                .thenApplyAsync(imageEntityOptional ->
                        imageEntityOptional.map(imageEntity ->
                                new Image(imageEntity.getId(), imageEntity.getName(), imageEntity.getUrl())
                        )
                );
//                .thenApply(imageEntityOptional -> {                                   // 람다식이 한줄이면, 중괄호랑 return 구문 생략 가능
//                    return imageEntityOptional.map(imageEntity -> {
//                        return new Image(imageEntity.getId(), imageEntity.getName(), imageEntity.getUrl());
//                    });

        // List<ArticleEntity> -> List<Article>
        CompletableFuture<List<Article>> articlesFuture = articleComRepository.findAllByUserId(userEntity.getId())
                .thenApplyAsync(articleEntityList ->
                        articleEntityList.stream()
                                .map(articleEntity ->
                                        new Article(articleEntity.getId(), articleEntity.getTitle(), articleEntity.getContent())
                                )
                                .collect(Collectors.toList())
                );

        CompletableFuture<Long> followCountFuture = followComRepository.countByUserId(userEntity.getId());



        // 각 future의 작업이 완료된 시점을 보장
        // allOf를 활용하여 return문 변경.
        // allOf에서 작업이 완료된 시점에 thenApply 실행. thenApply에서는 Optional<User> 리턴
        // 결과론적으로 getUser()의 리턴은 CompletableFuture<Optional<User>>이기 때문에
        return CompletableFuture.allOf(imageFuture, articlesFuture, followCountFuture)
                .thenApply(v ->{                    // allOf는 null반환이라 v는 null
                    try {
                        Optional<Image> image = imageFuture.get();
                        List<Article> articles = articlesFuture.get();
                        Long followCount = followCountFuture.get();

                        return Optional.of(
                                new User(                            // 결과도 Optional로 감싸야함
                                        userEntity.getId(),
                                        userEntity.getName(),
                                        userEntity.getAge(),
                                        image,
                                        articles,
                                        followCount
                                )
                        );
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });

//        return CompletableFuture.completedFuture(
//                Optional.of(
//                        new User(                            // 결과도 Optional로 감싸야함
//                                userEntity.getId(),
//                                userEntity.getName(),
//                                userEntity.getAge(),
//                                image,
//                                articles,
//                                followCount
//                        )
//                )
//        );
    }
}
