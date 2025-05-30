package com.mylearning.userservice.service;

import com.mylearning.userservice.dto.UserRequestDto;
import com.mylearning.userservice.dto.UserResponseDto;
import com.mylearning.userservice.entity.User;
import com.mylearning.userservice.exception.UserAlreadyExistsException;
import com.mylearning.userservice.exception.UserNotFoundException;
import com.mylearning.userservice.repository.UserRepository;
import com.mylearning.userservice.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponseDto register(UserRequestDto requestDto) {
        // Check if username already exists
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + requestDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + requestDto.getEmail());
        }

        User user = UserMapper.toUser(requestDto);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        return UserMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getProfileByUsername(String username) {
        return UserMapper.toUserResponseDto(userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username)));
    }

    @Override
    public UserResponseDto getProfileByEmail(String email) {
        return UserMapper.toUserResponseDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email)));
    }
}
