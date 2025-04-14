package com.grizz.wooman.completablefuture.completableFuture.blocking;

import com.grizz.wooman.completablefuture.common.User;
import com.grizz.wooman.completablefuture.completableFuture.UserComService;
import com.grizz.wooman.completablefuture.completableFuture.repository.ArticleComRepository;
import com.grizz.wooman.completablefuture.completableFuture.repository.FollowComRepository;
import com.grizz.wooman.completablefuture.completableFuture.repository.ImageComRepository;
import com.grizz.wooman.completablefuture.completableFuture.repository.UserComRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceComTest {
    UserComService userComService;
    UserComRepository userRepository;
    ArticleComRepository articleRepository;
    ImageComRepository imageRepository;
    FollowComRepository followRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserComRepository();
        articleRepository = new ArticleComRepository();
        imageRepository = new ImageComRepository();
        followRepository = new FollowComRepository();

        userComService = new UserComService(
                userRepository, articleRepository, imageRepository, followRepository
        );
    }

    @Test
    void getUserEmptyIfInvalidUserIdIsGiven() throws ExecutionException, InterruptedException {
        // given
        String userId = "invalid_user_id";

        // when
        Optional<User> user = userComService.getUserById(userId).get();         //CompletableFuture를 반환하기 때문에 get()으로 가져와야함

        // then
        assertTrue(user.isEmpty());
    }

    @Test
    void testGetUser() throws ExecutionException, InterruptedException {
        // given
        String userId = "1234";
        // when
        Optional<User> optionalUser = userComService.getUserById(userId).get();

        // then
        assertFalse(optionalUser.isEmpty());
        var user = optionalUser.get();
        assertEquals(user.getName(), "lkm");
        assertEquals(user.getAge(), 32);

        assertFalse(user.getProfileImage().isEmpty());
        var image = user.getProfileImage().get();
        assertEquals(image.getId(), "image#1000");
        assertEquals(image.getName(), "profileImage");
        assertEquals(image.getUrl(), "https://www.naver.com");

        assertEquals(2, user.getArticleList().size());

        assertEquals(1000, user.getFollowCount());
    }
}
