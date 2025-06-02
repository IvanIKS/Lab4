package com.lab41.service;

import com.lab41.model.Comment;
import com.lab41.model.Like;
import com.lab41.model.Post;
import com.lab41.model.User;
import com.lab41.repository.CommentRepository;
import com.lab41.repository.LikeRepository;
import com.lab41.repository.PostRepository;
import com.lab41.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostRepository postRepository,
                       CommentRepository commentRepository,
                       LikeRepository likeRepository,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public Post createPost(Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));

        Post post = new Post();
        post.setUser(user);
        post.setContent(content);
        return postRepository.save(post);
    }


    public Optional<Post> getPostById(Long postId) {
        return postRepository.findById(postId);
    }

    public List<Post> getAllPosts() {
        return (List<Post>) postRepository.findAll();
    }

    public List<Post> getPostsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));
        return postRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Post updatePost(Long postId, String newContent) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post with ID " + postId + " not found."));

        post.setContent(newContent);
        return postRepository.save(post);
    }


    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post with ID " + postId + " not found for deletion.");
        }
        postRepository.deleteById(postId);
    }

    @Transactional
    public Comment addCommentToPost(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post with ID " + postId + " not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }


    public List<Comment> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post with ID " + postId + " not found."));
        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }


    @Transactional
    public Comment updateComment(Long commentId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment with ID " + commentId + " not found."));

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }


    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new IllegalArgumentException("Comment with ID " + commentId + " not found for deletion.");
        }
        commentRepository.deleteById(commentId);
    }


    @Transactional
    public Like addLikeToPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post with ID " + postId + " not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));

        if (likeRepository.findByUserAndPost(user, post).isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " has already liked post with ID " + postId + ".");
        }

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        return likeRepository.save(like);
    }


    public Optional<Like> getLikeById(Long likeId) {
        return likeRepository.findById(likeId);
    }


    public List<Like> getLikesByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post with ID " + postId + " not found."));
        return likeRepository.findByPost(post);
    }

    public List<Like> getLikesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));
        return likeRepository.findByUser(user);
    }



    @Transactional
    public void removeLikeFromPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post with ID " + postId + " not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));

        Like like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new IllegalArgumentException("Like from user " + userId + " on post " + postId + " not found."));

        likeRepository.delete(like);
    }

    public List<Post> searchPostsByContent(String keyword) {
        return postRepository.findByContentContainingIgnoreCase(keyword);
    }

    public List<Post> getPostsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return postRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }

    public List<Post> getTopPostsForUserByLikes(Long userId, int limit) {
        Optional<User> user= userRepository.findById(userId);
        List<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user.get());

        return posts
                .stream()
                .sorted((post1, post2) ->
                                Integer.compare(
                                        likeRepository.findByPost(post1).size(),
                                        likeRepository.findByPost(post2).size()))
                .limit(limit)
                .collect(Collectors.toList());
    }


}
