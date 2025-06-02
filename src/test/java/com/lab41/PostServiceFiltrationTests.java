package com.lab41;

import com.lab41.model.Comment;
import com.lab41.model.Like;
import com.lab41.model.Post;
import com.lab41.model.User;
import com.lab41.repository.CommentRepository;
import com.lab41.repository.LikeRepository;
import com.lab41.repository.PostRepository;
import com.lab41.repository.UserRepository;
import com.lab41.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceFiltrationTests {

    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private User anotherUser;
    private Post testPost;
    private Post postWithLikes;
    private Post postWithoutLikes;
    private Comment testComment;
    private Like testLike;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setCreatedAt(LocalDateTime.now());

        anotherUser = new User();
        anotherUser.setUserId(2L);
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordHash("hashedanotherpassword");
        anotherUser.setCreatedAt(LocalDateTime.now());

        testPost = new Post();
        testPost.setPostId(101L);
        testPost.setUser(testUser);
        testPost.setContent("This is a test post content.");
        testPost.setCreatedAt(LocalDateTime.now().minusHours(5));

        postWithLikes = new Post();
        postWithLikes.setPostId(102L);
        postWithLikes.setUser(testUser);
        postWithLikes.setContent("Post with many likes.");
        postWithLikes.setCreatedAt(LocalDateTime.now().minusHours(2));

        postWithoutLikes = new Post();
        postWithoutLikes.setPostId(103L);
        postWithoutLikes.setUser(testUser);
        postWithoutLikes.setContent("Post with no likes.");
        postWithoutLikes.setCreatedAt(LocalDateTime.now().minusHours(1));


        testComment = new Comment();
        testComment.setCommentId(201L);
        testComment.setPost(testPost);
        testComment.setUser(testUser);
        testComment.setContent("This is a test comment.");
        testComment.setCreatedAt(LocalDateTime.now());

        testLike = new Like();
        testLike.setLikeId(301L);
        testLike.setPost(testPost);
        testLike.setUser(testUser);
        testLike.setCreatedAt(LocalDateTime.now());
    }


    @Test
    @DisplayName("searchPostsByContent: Should find posts containing the keyword (case-insensitive)")
    void searchPostsByContent_KeywordFound_ReturnsMatchingPosts() {
        String keyword = "test";
        Post post1 = new Post();
        post1.setContent("This is a Test post.");
        Post post2 = new Post();
        post2.setContent("Another post with test keyword.");
        List<Post> expectedPosts = Arrays.asList(post1, post2);

        when(postRepository.findByContentContainingIgnoreCase(keyword)).thenReturn(expectedPosts);

        List<Post> foundPosts = postService.searchPostsByContent(keyword);

        assertNotNull(foundPosts);
        assertEquals(2, foundPosts.size());
        assertTrue(foundPosts.containsAll(expectedPosts));
        verify(postRepository, times(1)).findByContentContainingIgnoreCase(keyword);
    }

    @Test
    @DisplayName("searchPostsByContent: Should return empty list if no posts match the keyword")
    void searchPostsByContent_NoKeywordMatch_ReturnsEmptyList() {
        String keyword = "nonexistent";
        when(postRepository.findByContentContainingIgnoreCase(keyword)).thenReturn(Collections.emptyList());

        List<Post> foundPosts = postService.searchPostsByContent(keyword);

        assertNotNull(foundPosts);
        assertTrue(foundPosts.isEmpty());
        verify(postRepository, times(1)).findByContentContainingIgnoreCase(keyword);
    }

    @Test
    @DisplayName("getPostsBetweenDates: Should return posts created within the specified date range")
    void getPostsBetweenDates_PostsExistInRange_ReturnsMatchingPosts() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        Post post1 = new Post();
        post1.setPostId(200L);
        post1.setCreatedAt(LocalDateTime.now().minusDays(5));
        post1.setContent("Post from 5 days ago");

        Post post2 = new Post();
        post2.setPostId(201L);
        post2.setCreatedAt(LocalDateTime.now().minusHours(12));
        post2.setContent("Post from 12 hours ago");

        List<Post> expectedPosts = Arrays.asList(post1, post2);

        when(postRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate)).thenReturn(expectedPosts);

        List<Post> foundPosts = postService.getPostsBetweenDates(startDate, endDate);

        assertNotNull(foundPosts);
        assertEquals(2, foundPosts.size());
        assertTrue(foundPosts.containsAll(expectedPosts));
        verify(postRepository, times(1)).findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }

    @Test
    @DisplayName("getPostsBetweenDates: Should return empty list if no posts within the date range")
    void getPostsBetweenDates_NoPostsInRange_ReturnsEmptyList() {
        LocalDateTime startDate = LocalDateTime.now().minusYears(2);
        LocalDateTime endDate = LocalDateTime.now().minusYears(1);

        when(postRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate)).thenReturn(Collections.emptyList());

        List<Post> foundPosts = postService.getPostsBetweenDates(startDate, endDate);

        assertNotNull(foundPosts);
        assertTrue(foundPosts.isEmpty());
        verify(postRepository, times(1)).findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }


    @Test
    @DisplayName("getTopPostsForUserByLikes: Should return all posts if user has fewer posts than limit")
    void getTopPostsForUserByLikes_FewerPostsThanLimit_ReturnsAllPosts() {
        User user = testUser;
        int limit = 5;

        Post post1 = new Post(); post1.setPostId(1L); post1.setUser(user); post1.setContent("Post 1");
        Post post2 = new Post(); post2.setPostId(2L); post2.setUser(user); post2.setContent("Post 2");

        List<Post> usersPosts = Arrays.asList(post1, post2);
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(postRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(usersPosts);

        when(likeRepository.findByPost(post1)).thenReturn(Collections.singletonList(new Like()));
        when(likeRepository.findByPost(post2)).thenReturn(Arrays.asList(new Like(), new Like()));

        // Act
        List<Post> topPosts = postService.getTopPostsForUserByLikes(user.getUserId(), limit);

        // Assert
        assertNotNull(topPosts);
        assertEquals(2, topPosts.size()); // Should return only the available posts
        assertEquals(post2, topPosts.get(0)); // Post 2 has more likes (2 vs 1)
        assertEquals(post1, topPosts.get(1));

        verify(userRepository, times(1)).findById(user.getUserId());
        verify(postRepository, times(1)).findByUserOrderByCreatedAtDesc(user);
        verify(likeRepository, times(1)).findByPost(post1);
        verify(likeRepository, times(1)).findByPost(post2);
    }

    @Test
    @DisplayName("getTopPostsForUserByLikes: Should return empty list if user has no posts")
    void getTopPostsForUserByLikes_NoPosts_ReturnsEmptyList() {
        User user = testUser;
        int limit = 3;

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(postRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(Collections.emptyList()); // User has no posts

        List<Post> topPosts = postService.getTopPostsForUserByLikes(user.getUserId(), limit);

        assertNotNull(topPosts);
        assertTrue(topPosts.isEmpty());

        verify(userRepository, times(1)).findById(user.getUserId());
        verify(postRepository, times(1)).findByUserOrderByCreatedAtDesc(user);
        verify(likeRepository, never()).findByPost(any(Post.class)); // No posts, so no likes checked
    }

    @Test
    @DisplayName("getTopPostsForUserByLikes: Should throw IllegalArgumentException if user not found")
    void getTopPostsForUserByLikes_UserNotFound_ThrowsException() {
        Long nonExistentUserId = 99L;
        int limit = 3;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.getTopPostsForUserByLikes(nonExistentUserId, limit);
        });
        assertEquals("User with ID " + nonExistentUserId + " not found.", thrown.getMessage());
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(postRepository, never()).findByUserOrderByCreatedAtDesc(any(User.class));
        verify(likeRepository, never()).findByPost(any(Post.class));
    }
}