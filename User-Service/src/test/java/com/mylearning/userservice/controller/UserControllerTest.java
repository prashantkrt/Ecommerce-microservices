package com.mylearning.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.userservice.dto.UserRequestDto;
import com.mylearning.userservice.dto.UserResponseDto;
import com.mylearning.userservice.exception.GlobalExceptionHandler;
import com.mylearning.userservice.exception.UserNotFoundException;
import com.mylearning.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserResponseDto userResponseDto;
    private UserRequestDto userRequestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRequestDto = UserRequestDto.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    void register_ShouldReturnCreatedUser() throws Exception {
        when(userService.register(any(UserRequestDto.class))).thenReturn(userResponseDto);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(userResponseDto.getUsername())))
                .andExpect(jsonPath("$.email", is(userResponseDto.getEmail())));

        verify(userService, times(1)).register(any(UserRequestDto.class));
    }

    @Test
    void getProfileByUsername_WhenUserExists_ShouldReturnUser() throws Exception {
        when(userService.getProfileByUsername("testuser")).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/users/profile/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).getProfileByUsername("testuser");
    }

    @Test
    void getProfileByUsername_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        when(userService.getProfileByUsername("nonexistent"))
                .thenThrow(new UserNotFoundException("User not found with username: nonexistent"));

        mockMvc.perform(get("/api/users/profile/username/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found with username: nonexistent")));

        verify(userService, times(1)).getProfileByUsername("nonexistent");
    }

    @Test
    void register_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        UserRequestDto invalidUser = new UserRequestDto();
        
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(userService, never()).register(any(UserRequestDto.class));
    }
    
    @Test
    void getProfileByEmail_WhenUserExists_ShouldReturnUser() throws Exception {
        when(userService.getProfileByEmail("test@example.com")).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/users/profile/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).getProfileByEmail("test@example.com");
    }
}
