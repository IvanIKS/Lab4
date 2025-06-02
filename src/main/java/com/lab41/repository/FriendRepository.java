package com.lab41.repository;

import com.lab41.model.Friend;
import com.lab41.model.FriendStatus;
import com.lab41.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends CrudRepository<Friend, Long> {
    Optional<Friend> findByUserAndFriend(User user, User friend);

    Optional<Friend> findByUserAndFriendAndStatus(User user, User friend, FriendStatus status);

    List<Friend> findByUserAndStatus(User user, FriendStatus status);

    List<Friend> findByFriendAndStatus(User friend, FriendStatus status);

    List<Friend> findByUserOrFriendAndStatus(User user, User friend, FriendStatus status);

}
