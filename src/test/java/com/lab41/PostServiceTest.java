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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

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
    private Post testPost;
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

        testPost = new Post();
        testPost.setPostId(101L);
        testPost.setUser(testUser);
        testPost.setContent("This is a test post content.");
        testPost.setCreatedAt(LocalDateTime.now());

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
    @DisplayName("Should create a new post successfully")
    void createPost_Success() {
        String postContent = "New post content";
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(testPost); // Mock saving the post

        Post createdPost = postService.createPost(testUser.getUserId(), postContent);

        assertNotNull(createdPost);
        assertEquals(testUser, createdPost.getUser());
        assertEquals(testPost.getContent(), createdPost.getContent()); // The content might be the mocked one
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating post for non-existent user")
    void createPost_UserNotFound_ThrowsException() {
        Long nonExistentUserId = 99L;
        String postContent = "Some content";
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.createPost(nonExistentUserId, postContent);
        });
        assertEquals("User with ID " + nonExistentUserId + " not found.", thrown.getMessage());
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(postRepository, never()).save(any(Post.class)); // Ensure save is not called
    }

    @Test
    @DisplayName("Should get a post by ID when it exists")
    void getPostById_PostExists_ReturnsOptionalPost() {
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));

        Optional<Post> foundPost = postService.getPostById(testPost.getPostId());

        assertTrue(foundPost.isPresent());
        assertEquals(testPost.getContent(), foundPost.get().getContent());
        verify(postRepository, times(1)).findById(testPost.getPostId());
    }

    @Test
    @DisplayName("Should return empty optional when post ID does not exist")
    void getPostById_PostDoesNotExist_ReturnsEmptyOptional() {
        Long nonExistentPostId = 999L;
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        Optional<Post> foundPost = postService.getPostById(nonExistentPostId);

        assertFalse(foundPost.isPresent());
        verify(postRepository, times(1)).findById(nonExistentPostId);
    }

    @Test
    @DisplayName("Should retrieve all posts successfully")
    void getAllPosts_ReturnsListOfPosts() {
        Post post2 = new Post();
        post2.setPostId(102L);
        post2.setUser(testUser);
        post2.setContent("Another post.");

        List<Post> posts = Arrays.asList(testPost, post2);
        when(postRepository.findAll()).thenReturn(posts);
        List<Post> allPosts = postService.getAllPosts();

        assertNotNull(allPosts);
        assertEquals(2, allPosts.size());
        assertTrue(allPosts.contains(testPost));
        assertTrue(allPosts.contains(post2));
        verify(postRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve posts by user successfully")
    void getPostsByUser_UserExists_ReturnsListOfPosts() {
        Post userPost1 = new Post();
        userPost1.setPostId(103L);
        userPost1.setUser(testUser);
        userPost1.setContent("User's post 1.");

        List<Post> userPosts = Arrays.asList(testPost, userPost1);
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(postRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(userPosts);

        List<Post> foundPosts = postService.getPostsByUser(testUser.getUserId());

        assertNotNull(foundPosts);
        assertEquals(2, foundPosts.size());
        assertTrue(foundPosts.contains(testPost));
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(postRepository, times(1)).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when getting posts for non-existent user")
    void getPostsByUser_UserNotFound_ThrowsException() {
        Long nonExistentUserId = 99L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.getPostsByUser(nonExistentUserId);
        });
        assertEquals("User with ID " + nonExistentUserId + " not found.", thrown.getMessage());
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(postRepository, never()).findByUserOrderByCreatedAtDesc(any(User.class));
    }

    @Test
    @DisplayName("Should update an existing post successfully")
    void updatePost_PostExists_ReturnsUpdatedPost() {
        String updatedContent = "Updated post content.";
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost); // Mock save returns the updated object

        Post updatedPost = postService.updatePost(testPost.getPostId(), updatedContent);

        assertNotNull(updatedPost);
        assertEquals(updatedContent, updatedPost.getContent()); // Ensure content is updated
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(postRepository, times(1)).save(testPost); // Verify save was called with the modified post
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating non-existent post")
    void updatePost_PostDoesNotExist_ThrowsException() {
        Long nonExistentPostId = 999L;
        String updatedContent = "New content";
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.updatePost(nonExistentPostId, updatedContent);
        });
        assertEquals("Post with ID " + nonExistentPostId + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(nonExistentPostId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should delete a post by ID successfully")
    void deletePost_PostExists_PerformsDeletion() {
        when(postRepository.existsById(testPost.getPostId())).thenReturn(true);
        doNothing().when(postRepository).deleteById(testPost.getPostId());

        postService.deletePost(testPost.getPostId());

        verify(postRepository, times(1)).existsById(testPost.getPostId());
        verify(postRepository, times(1)).deleteById(testPost.getPostId());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when deleting non-existent post")
    void deletePost_PostDoesNotExist_ThrowsException() {
        Long nonExistentPostId = 999L;
        when(postRepository.existsById(nonExistentPostId)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.deletePost(nonExistentPostId);
        });
        assertEquals("Post with ID " + nonExistentPostId + " not found for deletion.", thrown.getMessage());
        verify(postRepository, times(1)).existsById(nonExistentPostId);
        verify(postRepository, never()).deleteById(anyLong()); // Ensure delete is not called
    }

    // --- Comment CRUD Tests ---

    @Test
    @DisplayName("Should add a comment to a post successfully")
    void addCommentToPost_Success() {
        String commentContent = "Great post!";
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment addedComment = postService.addCommentToPost(testPost.getPostId(), testUser.getUserId(), commentContent);

        assertNotNull(addedComment);
        assertEquals(testPost, addedComment.getPost());
        assertEquals(testUser, addedComment.getUser());
        assertEquals(testComment.getContent(), addedComment.getContent());
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when adding comment to non-existent post")
    void addCommentToPost_PostNotFound_ThrowsException() {
        Long nonExistentPostId = 999L;
        String commentContent = "Content";
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.addCommentToPost(nonExistentPostId, testUser.getUserId(), commentContent);
        });
        assertEquals("Post with ID " + nonExistentPostId + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(nonExistentPostId);
        verify(userRepository, never()).findById(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when adding comment with non-existent user")
    void addCommentToPost_UserNotFound_ThrowsException() {
        Long nonExistentUserId = 99L;
        String commentContent = "Content";
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.addCommentToPost(testPost.getPostId(), nonExistentUserId, commentContent);
        });
        assertEquals("User with ID " + nonExistentUserId + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should get a comment by ID when it exists")
    void getCommentById_CommentExists_ReturnsOptionalComment() {
        when(commentRepository.findById(testComment.getCommentId())).thenReturn(Optional.of(testComment));

        Optional<Comment> foundComment = postService.getCommentById(testComment.getCommentId());

        assertTrue(foundComment.isPresent());
        assertEquals(testComment.getContent(), foundComment.get().getContent());
        verify(commentRepository, times(1)).findById(testComment.getCommentId());
    }

    @Test
    @DisplayName("Should return empty optional when comment ID does not exist")
    void getCommentById_CommentDoesNotExist_ReturnsEmptyOptional() {
        Long nonExistentCommentId = 999L;
        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        Optional<Comment> foundComment = postService.getCommentById(nonExistentCommentId);

        assertFalse(foundComment.isPresent());
        verify(commentRepository, times(1)).findById(nonExistentCommentId);
    }

    @Test
    @DisplayName("Should retrieve comments by post successfully")
    void getCommentsByPost_PostExists_ReturnsListOfComments() {
        Comment comment2 = new Comment();
        comment2.setCommentId(202L);
        comment2.setPost(testPost);
        comment2.setUser(testUser);
        comment2.setContent("Another comment.");
        List<Comment> comments = Arrays.asList(testComment, comment2);

        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(commentRepository.findByPostOrderByCreatedAtAsc(testPost)).thenReturn(comments);

        List<Comment> foundComments = postService.getCommentsByPost(testPost.getPostId());

        assertNotNull(foundComments);
        assertEquals(2, foundComments.size());
        assertTrue(foundComments.contains(testComment));
        assertTrue(foundComments.contains(comment2));
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(commentRepository, times(1)).findByPostOrderByCreatedAtAsc(testPost);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when getting comments for non-existent post")
    void getCommentsByPost_PostNotFound_ThrowsException() {
        Long nonExistentPostId = 999L;
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.getCommentsByPost(nonExistentPostId);
        });
        assertEquals("Post with ID " + nonExistentPostId + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(nonExistentPostId);
        verify(commentRepository, never()).findByPostOrderByCreatedAtAsc(any(Post.class));
    }

    @Test
    @DisplayName("Should update an existing comment successfully")
    void updateComment_CommentExists_ReturnsUpdatedComment() {
        String updatedContent = "Updated comment content.";
        when(commentRepository.findById(testComment.getCommentId())).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment updatedComment = postService.updateComment(testComment.getCommentId(), updatedContent);

        assertNotNull(updatedComment);
        assertEquals(updatedContent, updatedComment.getContent());
        verify(commentRepository, times(1)).findById(testComment.getCommentId());
        verify(commentRepository, times(1)).save(testComment);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating non-existent comment")
    void updateComment_CommentDoesNotExist_ThrowsException() {
        Long nonExistentCommentId = 999L;
        String updatedContent = "New content";
        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.updateComment(nonExistentCommentId, updatedContent);
        });
        assertEquals("Comment with ID " + nonExistentCommentId + " not found.", thrown.getMessage());
        verify(commentRepository, times(1)).findById(nonExistentCommentId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should delete a comment by ID successfully")
    void deleteComment_CommentExists_PerformsDeletion() {
        when(commentRepository.existsById(testComment.getCommentId())).thenReturn(true);
        doNothing().when(commentRepository).deleteById(testComment.getCommentId());

        postService.deleteComment(testComment.getCommentId());

        verify(commentRepository, times(1)).existsById(testComment.getCommentId());
        verify(commentRepository, times(1)).deleteById(testComment.getCommentId());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when deleting non-existent comment")
    void deleteComment_CommentDoesNotExist_ThrowsException() {
        Long nonExistentCommentId = 999L;
        when(commentRepository.existsById(nonExistentCommentId)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.deleteComment(nonExistentCommentId);
        });
        assertEquals("Comment with ID " + nonExistentCommentId + " not found for deletion.", thrown.getMessage());
        verify(commentRepository, times(1)).existsById(nonExistentCommentId);
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should add a like to a post successfully")
    void addLikeToPost_Success() {
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty()); // No existing like
        when(likeRepository.save(any(Like.class))).thenReturn(testLike);

        Like addedLike = postService.addLikeToPost(testPost.getPostId(), testUser.getUserId());

        assertNotNull(addedLike);
        assertEquals(testPost, addedLike.getPost());
        assertEquals(testUser, addedLike.getUser());
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(likeRepository, times(1)).findByUserAndPost(testUser, testPost);
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when adding like to non-existent post")
    void addLikeToPost_PostNotFound_ThrowsException() {
        Long nonExistentPostId = 999L;
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.addLikeToPost(nonExistentPostId, testUser.getUserId());
        });
        assertEquals("Post with ID " + nonExistentPostId + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(nonExistentPostId);
        verify(userRepository, never()).findById(anyLong());
        verify(likeRepository, never()).findByUserAndPost(any(User.class), any(Post.class));
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when adding like with non-existent user")
    void addLikeToPost_UserNotFound_ThrowsException() {
        Long nonExistentUserId = 99L;
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.addLikeToPost(testPost.getPostId(), nonExistentUserId);
        });
        assertEquals("User with ID " + nonExistentUserId + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(likeRepository, never()).findByUserAndPost(any(User.class), any(Post.class));
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user has already liked the post")
    void addLikeToPost_AlreadyLiked_ThrowsException() {
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(testLike)); // Existing like found

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.addLikeToPost(testPost.getPostId(), testUser.getUserId());
        });
        assertEquals("User with ID " + testUser.getUserId() + " has already liked post with ID " + testPost.getPostId() + ".", thrown.getMessage());
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(likeRepository, times(1)).findByUserAndPost(testUser, testPost);
        verify(likeRepository, never()).save(any(Like.class)); // Save should not be called
    }

    @Test
    @DisplayName("Should get a like by ID when it exists")
    void getLikeById_LikeExists_ReturnsOptionalLike() {
        when(likeRepository.findById(testLike.getLikeId())).thenReturn(Optional.of(testLike));

        Optional<Like> foundLike = postService.getLikeById(testLike.getLikeId());

        assertTrue(foundLike.isPresent());
        assertEquals(testLike.getUser(), foundLike.get().getUser());
        verify(likeRepository, times(1)).findById(testLike.getLikeId());
    }

    @Test
    @DisplayName("Should return empty optional when like ID does not exist")
    void getLikeById_LikeDoesNotExist_ReturnsEmptyOptional() {
        Long nonExistentLikeId = 999L;
        when(likeRepository.findById(nonExistentLikeId)).thenReturn(Optional.empty());

        Optional<Like> foundLike = postService.getLikeById(nonExistentLikeId);

        assertFalse(foundLike.isPresent());
        verify(likeRepository, times(1)).findById(nonExistentLikeId);
    }

    @Test
    @DisplayName("Should retrieve likes by post successfully")
    void getLikesByPost_PostExists_ReturnsListOfLikes() {
        Like like2 = new Like();
        like2.setLikeId(302L);
        like2.setPost(testPost);
        like2.setUser(new User()); // Another user
        List<Like> likes = Arrays.asList(testLike, like2);

        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(likeRepository.findByPost(testPost)).thenReturn(likes);

        List<Like> foundLikes = postService.getLikesByPost(testPost.getPostId());

        assertNotNull(foundLikes);
        assertEquals(2, foundLikes.size());
        assertTrue(foundLikes.contains(testLike));
        assertTrue(foundLikes.contains(like2));
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(likeRepository, times(1)).findByPost(testPost);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when getting likes for non-existent post")
    void getLikesByPost_PostNotFound_ThrowsException() {
        Long nonExistentPostId = 999L;
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.getLikesByPost(nonExistentPostId);
        });
        assertEquals("Post with ID " + nonExistentPostId + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(nonExistentPostId);
        verify(likeRepository, never()).findByPost(any(Post.class));
    }

    @Test
    @DisplayName("Should retrieve likes by user successfully")
    void getLikesByUser_UserExists_ReturnsListOfLikes() {
        Like like2 = new Like();
        like2.setLikeId(302L);
        like2.setPost(new Post());
        like2.setUser(testUser);
        List<Like> likes = Arrays.asList(testLike, like2);

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(likeRepository.findByUser(testUser)).thenReturn(likes);

        List<Like> foundLikes = postService.getLikesByUser(testUser.getUserId());

        assertNotNull(foundLikes);
        assertEquals(2, foundLikes.size());
        assertTrue(foundLikes.contains(testLike));
        assertTrue(foundLikes.contains(like2));
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(likeRepository, times(1)).findByUser(testUser);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when getting likes for non-existent user")
    void getLikesByUser_UserNotFound_ThrowsException() {
        Long nonExistentUserId = 99L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.getLikesByUser(nonExistentUserId);
        });
        assertEquals("User with ID " + nonExistentUserId + " not found.", thrown.getMessage());
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(likeRepository, never()).findByUser(any(User.class));
    }

    @Test
    @DisplayName("Should remove a like from a post successfully")
    void removeLikeFromPost_Success() {
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(testLike));
        doNothing().when(likeRepository).delete(testLike);

        postService.removeLikeFromPost(testPost.getPostId(), testUser.getUserId());

        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(likeRepository, times(1)).findByUserAndPost(testUser, testPost);
        verify(likeRepository, times(1)).delete(testLike);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when removing like from non-existent post")
    void removeLikeFromPost_PostNotFound_ThrowsException() {
        Long nonExistentPostId = 999L;
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.removeLikeFromPost(nonExistentPostId, testUser.getUserId());
        });
        assertEquals("Post with ID " + nonExistentPostId + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(nonExistentPostId);
        verify(userRepository, never()).findById(anyLong());
        verify(likeRepository, never()).findByUserAndPost(any(User.class), any(Post.class));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when removing like by non-existent user")
    void removeLikeFromPost_UserNotFound_ThrowsException() {
        Long nonExistentUserId = 99L;
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.removeLikeFromPost(testPost.getPostId(), nonExistentUserId);
        });
        assertEquals("User with ID " + nonExistentUserId + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(likeRepository, never()).findByUserAndPost(any(User.class), any(Post.class));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when removing non-existent like")
    void removeLikeFromPost_LikeNotFound_ThrowsException() {
        when(postRepository.findById(testPost.getPostId())).thenReturn(Optional.of(testPost));
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty()); // Like not found

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            postService.removeLikeFromPost(testPost.getPostId(), testUser.getUserId());
        });
        assertEquals("Like from user " + testUser.getUserId() + " on post " + testPost.getPostId() + " not found.", thrown.getMessage());
        verify(postRepository, times(1)).findById(testPost.getPostId());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(likeRepository, times(1)).findByUserAndPost(testUser, testPost);
        verify(likeRepository, never()).delete(any(Like.class)); // Delete should not be called
    }
}