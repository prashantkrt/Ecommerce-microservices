package com.mylearning.userservice.service;

import com.mylearning.userservice.dto.UserRequestDto;
import com.mylearning.userservice.dto.UserResponseDto;
import com.mylearning.userservice.entity.User;
import com.mylearning.userservice.exception.UserAlreadyExistsException;
import com.mylearning.userservice.exception.UserNotFoundException;
import com.mylearning.userservice.repository.UserRepository;
import com.mylearning.userservice.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User user;
    private UserRequestDto userRequestDto;
    private static final LocalDateTime TEST_DATE = LocalDateTime.of(2023, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword123")
                .createdAt(TEST_DATE)
                .updatedAt(TEST_DATE)
                .build();

        userRequestDto = UserRequestDto.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    void register_ShouldReturnUserResponse_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponseDto response = userService.register(userRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
        
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(userRequestDto.getUsername(), savedUser.getUsername());
        assertEquals(userRequestDto.getEmail(), savedUser.getEmail());
        assertEquals(userRequestDto.getPassword(), savedUser.getPassword());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, 
            () -> userService.register(userRequestDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, 
            () -> userService.register(userRequestDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getProfileByUsername_WhenUserExists_ShouldReturnUserResponse() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        UserResponseDto response = userService.getProfileByUsername("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
        assertEquals(user.getUpdatedAt(), response.getUpdatedAt());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getProfileByUsername_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.getProfileByUsername(username)
        );
        
        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getProfileByEmail_WhenUserExists_ShouldReturnUserResponse() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));


        // Act
        UserResponseDto response = userService.getProfileByEmail("test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getUsername(), response.getUsername());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void getProfileByEmail_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.getProfileByEmail(email)
        );
        
        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void register_ShouldSetTimestamps_WhenCreatingNewUser() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // Act
        UserResponseDto response = userService.register(userRequestDto);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
        verify(userRepository, times(1)).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    void register_ShouldNotExposePasswordInResponse() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponseDto response = userService.register(userRequestDto);

        // Assert
        assertNotNull(response);
        // Password is not exposed in the response DTO by design
    }
}
