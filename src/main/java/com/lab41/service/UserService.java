package com.lab41.service;

import com.lab41.model.Friend;
import com.lab41.model.FriendStatus;
import com.lab41.model.User;
import com.lab41.repository.FriendRepository;
import com.lab41.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    @Autowired
    public UserService(UserRepository userRepository, FriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }


    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }


    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }


    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }


    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }


    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }


    public List<User> findAllUsers() {
        return (List<User>) userRepository.findAll();
    }


    @Transactional
    public Friend sendFriendRequest(User sender, User receiver) {
        if (sender.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself.");
        }

        // Check if a request already exists in either direction
        boolean requestExists = friendRepository.findByUserAndFriend(sender, receiver).isPresent() ||
                friendRepository.findByUserAndFriend(receiver, sender).isPresent();

        if (requestExists) {
            throw new IllegalArgumentException("Friend request already exists between these users.");
        }

        Friend friendRequest = new Friend();
        friendRequest.setUser(sender);
        friendRequest.setFriend(receiver);
        friendRequest.setStatus(FriendStatus.PENDING);
        return friendRepository.save(friendRequest);
    }


    @Transactional
    public Friend acceptFriendRequest(Long senderId, Long receiverId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found."));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found."));

        Optional<Friend> friendRequestOpt = friendRepository.findByUserAndFriendAndStatus(sender, receiver, FriendStatus.PENDING);

        if (friendRequestOpt.isEmpty()) {
            throw new IllegalArgumentException("Pending friend request not found from sender to receiver.");
        }

        Friend friendRequest = friendRequestOpt.get();
        friendRequest.setStatus(FriendStatus.ACCEPTED);
        return friendRepository.save(friendRequest);
    }


    @Transactional
    public void declineFriendRequest(Long senderId, Long receiverId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found."));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found."));

        Optional<Friend> friendRequestOpt = friendRepository.findByUserAndFriendAndStatus(sender, receiver, FriendStatus.PENDING);

        if (friendRequestOpt.isEmpty()) {
            throw new IllegalArgumentException("Pending friend request not found from sender to receiver.");
        }

        friendRepository.delete(friendRequestOpt.get());
    }


    @Transactional
    public void removeFriend(User user1, User user2) {
        Optional<Friend> friendship1 = friendRepository.findByUserAndFriendAndStatus(user1, user2, FriendStatus.ACCEPTED);
        Optional<Friend> friendship2 = friendRepository.findByUserAndFriendAndStatus(user2, user1, FriendStatus.ACCEPTED);

        if (friendship1.isPresent()) {
            friendRepository.delete(friendship1.get());
        } else if (friendship2.isPresent()) {
            friendRepository.delete(friendship2.get());
        } else {
            throw new IllegalArgumentException("No active friendship found between these users.");
        }
    }

}
