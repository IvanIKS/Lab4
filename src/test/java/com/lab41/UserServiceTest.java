package com.lab41;


import com.lab41.model.Friend;
import com.lab41.model.FriendStatus;
import com.lab41.model.User;
import com.lab41.repository.FriendRepository;
import com.lab41.repository.UserRepository;
import com.lab41.service.UserService;
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
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRepository friendRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User friendUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setCreatedAt(LocalDateTime.now());

        friendUser = new User();
        friendUser.setUserId(2L);
        friendUser.setUsername("frienduser");
        friendUser.setEmail("friend@example.com");
        friendUser.setPasswordHash("hashedfriendpassword");
        friendUser.setCreatedAt(LocalDateTime.now());

        anotherUser = new User();
        anotherUser.setUserId(3L);
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordHash("hashedanotherpassword");
        anotherUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should find user by ID when user exists")
    void findById_UserExists_ReturnsOptionalUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.findById(1L);

        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when user ID does not exist")
    void findById_UserDoesNotExist_ReturnsEmptyOptional() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findById(2L);

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should find user by username when user exists")
    void findByUsername_UserExists_ReturnsOptionalUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        Optional<User> foundUser = userService.findByUsername("testuser");

        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should return empty optional when username does not exist")
    void findByUsername_UserDoesNotExist_ReturnsEmptyOptional() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(null); // Mock returns null for not found

        Optional<User> foundUser = userService.findByUsername("nonexistent");

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should find user by email when user exists")
    void findByEmail_UserExists_ReturnsOptionalUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        Optional<User> foundUser = userService.findByEmail("test@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should return empty optional when email does not exist")
    void findByEmail_UserDoesNotExist_ReturnsEmptyOptional() {

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null); // Mock returns null for not found


        Optional<User> foundUser = userService.findByEmail("nonexistent@example.com");


        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should save a new user successfully")
    void saveUser_ValidUser_ReturnsSavedUser() {

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPasswordHash("newhashedpassword");

        when(userRepository.save(any(User.class))).thenReturn(testUser); // Mock save to return the testUser (or any user)

        User savedUser = userService.saveUser(newUser);

        assertNotNull(savedUser);
        assertEquals(testUser.getUsername(), savedUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class)); // Verify save was called with any User object
    }

    @Test
    @DisplayName("Should delete a user by ID successfully")
    void deleteUser_UserExists_PerformsDeletion() {

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should retrieve all users")
    void findAllUsers_ReturnsListOfUsers() {

        User user2 = new User();
        user2.setUserId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hash2");

        List<User> userList = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(userList);

        List<User> foundUsers = userService.findAllUsers();

        assertNotNull(foundUsers);
        assertEquals(2, foundUsers.size());
        assertTrue(foundUsers.contains(testUser));
        assertTrue(foundUsers.contains(user2));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("sendFriendRequest: Should send a friend request successfully")
    void sendFriendRequest_Success() {
        Friend expectedFriendRequest = new Friend();
        expectedFriendRequest.setUser(testUser);
        expectedFriendRequest.setFriend(friendUser);
        expectedFriendRequest.setStatus(FriendStatus.PENDING);

        when(friendRepository.findByUserAndFriend(testUser, friendUser)).thenReturn(Optional.empty());
        when(friendRepository.findByUserAndFriend(friendUser, testUser)).thenReturn(Optional.empty());
        when(friendRepository.save(any(Friend.class))).thenReturn(expectedFriendRequest); // Mock save

        Friend result = userService.sendFriendRequest(testUser, friendUser);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(friendUser, result.getFriend());
        assertEquals(FriendStatus.PENDING, result.getStatus());

        verify(friendRepository, times(1)).findByUserAndFriend(testUser, friendUser);
        verify(friendRepository, times(1)).findByUserAndFriend(friendUser, testUser);
        verify(friendRepository, times(1)).save(any(Friend.class));
    }

    @Test
    @DisplayName("sendFriendRequest: Should throw IllegalArgumentException when sending to self")
    void sendFriendRequest_ToSelf_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.sendFriendRequest(testUser, testUser);
        });

        assertEquals("Cannot send a friend request to yourself.", thrown.getMessage());
        verifyNoInteractions(friendRepository);
    }

    @Test
    @DisplayName("sendFriendRequest: Should throw IllegalArgumentException when request already exists (receiver to sender)")
    void sendFriendRequest_AlreadyExistsReverse_ThrowsException() {
        Friend existingRequest = new Friend();
        existingRequest.setUser(friendUser);
        existingRequest.setFriend(testUser);
        existingRequest.setStatus(FriendStatus.PENDING);

        when(friendRepository.findByUserAndFriend(testUser, friendUser)).thenReturn(Optional.empty());
        when(friendRepository.findByUserAndFriend(friendUser, testUser)).thenReturn(Optional.of(existingRequest));


        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.sendFriendRequest(testUser, friendUser);
        });

        assertEquals("Friend request already exists between these users.", thrown.getMessage());

        verify(friendRepository, times(1)).findByUserAndFriend(testUser, friendUser);
        verify(friendRepository, times(1)).findByUserAndFriend(friendUser, testUser);
        verify(friendRepository, never()).save(any(Friend.class));
    }

    @Test
    @DisplayName("acceptFriendRequest: Should accept a pending friend request successfully")
    void acceptFriendRequest_Success() {
        // Arrange
        Friend pendingRequest = new Friend();
        pendingRequest.setUser(testUser); // sender
        pendingRequest.setFriend(friendUser); // receiver
        pendingRequest.setStatus(FriendStatus.PENDING);

        Friend acceptedRequest = new Friend();
        acceptedRequest.setUser(testUser);
        acceptedRequest.setFriend(friendUser);
        acceptedRequest.setStatus(FriendStatus.ACCEPTED);

        // Mock repository calls
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUser.getUserId())).thenReturn(Optional.of(friendUser));
        when(friendRepository.findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.PENDING))
                .thenReturn(Optional.of(pendingRequest));
        when(friendRepository.save(any(Friend.class))).thenReturn(acceptedRequest);

        // Act
        Friend result = userService.acceptFriendRequest(testUser.getUserId(), friendUser.getUserId());

        // Assert
        assertNotNull(result);
        assertEquals(FriendStatus.ACCEPTED, result.getStatus());
        assertEquals(testUser, result.getUser());
        assertEquals(friendUser, result.getFriend());

        // Verify repository interactions
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(userRepository, times(1)).findById(friendUser.getUserId());
        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.PENDING);
        verify(friendRepository, times(1)).save(any(Friend.class));
    }

    @Test
    @DisplayName("acceptFriendRequest: Should throw IllegalArgumentException if sender not found")
    void acceptFriendRequest_SenderNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.empty()); // Sender not found

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.acceptFriendRequest(testUser.getUserId(), friendUser.getUserId());
        });

        assertEquals("Sender not found.", thrown.getMessage());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(userRepository, never()).findById(friendUser.getUserId()); // Receiver not checked
        verifyNoInteractions(friendRepository);
    }

    @Test
    @DisplayName("acceptFriendRequest: Should throw IllegalArgumentException if receiver not found")
    void acceptFriendRequest_ReceiverNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUser.getUserId())).thenReturn(Optional.empty()); // Receiver not found

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.acceptFriendRequest(testUser.getUserId(), friendUser.getUserId());
        });

        assertEquals("Receiver not found.", thrown.getMessage());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(userRepository, times(1)).findById(friendUser.getUserId());
        verifyNoInteractions(friendRepository);
    }

    @Test
    @DisplayName("acceptFriendRequest: Should throw IllegalArgumentException if pending request not found")
    void acceptFriendRequest_PendingRequestNotFound_ThrowsException() {
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUser.getUserId())).thenReturn(Optional.of(friendUser));
        // Mock that no pending request exists
        when(friendRepository.findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.PENDING))
                .thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.acceptFriendRequest(testUser.getUserId(), friendUser.getUserId());
        });

        assertEquals("Pending friend request not found from sender to receiver.", thrown.getMessage());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(userRepository, times(1)).findById(friendUser.getUserId());
        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.PENDING);
        verify(friendRepository, never()).save(any(Friend.class)); // Save should not be called
    }

    @Test
    @DisplayName("declineFriendRequest: Should decline a pending friend request successfully")
    void declineFriendRequest_Success() {
        // Arrange
        Friend pendingRequest = new Friend();
        pendingRequest.setUser(testUser); // sender
        pendingRequest.setFriend(friendUser); // receiver
        pendingRequest.setStatus(FriendStatus.PENDING);

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUser.getUserId())).thenReturn(Optional.of(friendUser));
        when(friendRepository.findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.PENDING))
                .thenReturn(Optional.of(pendingRequest));
        doNothing().when(friendRepository).delete(any(Friend.class)); // Mock void method

        userService.declineFriendRequest(testUser.getUserId(), friendUser.getUserId());


        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(userRepository, times(1)).findById(friendUser.getUserId());
        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.PENDING);
        verify(friendRepository, times(1)).delete(pendingRequest); // Verify delete was called with the correct object
    }

    @Test
    @DisplayName("declineFriendRequest: Should throw IllegalArgumentException if sender not found")
    void declineFriendRequest_SenderNotFound_ThrowsException() {
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.empty()); // Sender not found

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.declineFriendRequest(testUser.getUserId(), friendUser.getUserId());
        });

        assertEquals("Sender not found.", thrown.getMessage());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(userRepository, never()).findById(friendUser.getUserId());
        verifyNoInteractions(friendRepository);
    }

    @Test
    @DisplayName("declineFriendRequest: Should throw IllegalArgumentException if receiver not found")
    void declineFriendRequest_ReceiverNotFound_ThrowsException() {
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUser.getUserId())).thenReturn(Optional.empty()); // Receiver not found

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.declineFriendRequest(testUser.getUserId(), friendUser.getUserId());
        });

        assertEquals("Receiver not found.", thrown.getMessage());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(userRepository, times(1)).findById(friendUser.getUserId());
        verifyNoInteractions(friendRepository);
    }

    @Test
    @DisplayName("declineFriendRequest: Should throw IllegalArgumentException if pending request not found")
    void declineFriendRequest_PendingRequestNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUser.getUserId())).thenReturn(Optional.of(friendUser));
        // Mock that no pending request exists
        when(friendRepository.findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.PENDING))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.declineFriendRequest(testUser.getUserId(), friendUser.getUserId());
        });

        assertEquals("Pending friend request not found from sender to receiver.", thrown.getMessage());
        verify(userRepository, times(1)).findById(testUser.getUserId());
        verify(userRepository, times(1)).findById(friendUser.getUserId());
        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.PENDING);
        verify(friendRepository, never()).delete(any(Friend.class)); // Delete should not be called
    }

    @Test
    @DisplayName("removeFriend: Should remove a friendship successfully (user1 to user2)")
    void removeFriend_FriendshipExists1to2_RemovesFriendship() {
        // Arrange
        Friend acceptedFriendship1 = new Friend();
        acceptedFriendship1.setUser(testUser);
        acceptedFriendship1.setFriend(friendUser);
        acceptedFriendship1.setStatus(FriendStatus.ACCEPTED);

        // Mock that friendship exists from user1 to user2
        when(friendRepository.findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.ACCEPTED))
                .thenReturn(Optional.of(acceptedFriendship1));
        when(friendRepository.findByUserAndFriendAndStatus(friendUser, testUser, FriendStatus.ACCEPTED))
                .thenReturn(Optional.empty()); // Mock reverse check as not existing
        doNothing().when(friendRepository).delete(any(Friend.class));

        // Act
        userService.removeFriend(testUser, friendUser);

        // Assert
        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.ACCEPTED);
        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(friendUser, testUser, FriendStatus.ACCEPTED);
        verify(friendRepository, times(1)).delete(acceptedFriendship1); // Verify deletion of the found friendship
    }

    @Test
    @DisplayName("removeFriend: Should remove a friendship successfully (user2 to user1)")
    void removeFriend_FriendshipExists2to1_RemovesFriendship() {
        // Arrange
        Friend acceptedFriendship2 = new Friend();
        acceptedFriendship2.setUser(friendUser);
        acceptedFriendship2.setFriend(testUser);
        acceptedFriendship2.setStatus(FriendStatus.ACCEPTED);

        // Mock that friendship exists from user2 to user1
        when(friendRepository.findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.ACCEPTED))
                .thenReturn(Optional.empty()); // Mock forward check as not existing
        when(friendRepository.findByUserAndFriendAndStatus(friendUser, testUser, FriendStatus.ACCEPTED))
                .thenReturn(Optional.of(acceptedFriendship2));
        doNothing().when(friendRepository).delete(any(Friend.class));

        // Act
        userService.removeFriend(testUser, friendUser);

        // Assert
        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.ACCEPTED);
        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(friendUser, testUser, FriendStatus.ACCEPTED);
        verify(friendRepository, times(1)).delete(acceptedFriendship2); // Verify deletion of the found friendship
    }

    @Test
    @DisplayName("removeFriend: Should throw IllegalArgumentException if no active friendship found")
    void removeFriend_NoFriendship_ThrowsException() {

        when(friendRepository.findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.ACCEPTED))
                .thenReturn(Optional.empty());
        when(friendRepository.findByUserAndFriendAndStatus(friendUser, testUser, FriendStatus.ACCEPTED))
                .thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.removeFriend(testUser, friendUser);
        });

        assertEquals("No active friendship found between these users.", thrown.getMessage());

        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(testUser, friendUser, FriendStatus.ACCEPTED);
        verify(friendRepository, times(1)).findByUserAndFriendAndStatus(friendUser, testUser, FriendStatus.ACCEPTED);
        verify(friendRepository, never()).delete(any(Friend.class));
    }

}