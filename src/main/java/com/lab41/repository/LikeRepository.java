package com.lab41.repository;

import com.lab41.model.Like;
import com.lab41.model.Post;
import com.lab41.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends CrudRepository<Like, Long> {
    Optional<Like> findByUserAndPost (User user, Post post);

    List<Like> findByUser (User user);

    List<Like> findByPost (Post post);
}
