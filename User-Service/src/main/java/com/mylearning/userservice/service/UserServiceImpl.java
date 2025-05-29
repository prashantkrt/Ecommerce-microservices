package com.mylearning.userservice.service;

import com.mylearning.userservice.dto.UserRequestDto;
import com.mylearning.userservice.dto.UserResponseDto;
import com.mylearning.userservice.entity.User;
import com.mylearning.userservice.exception.UserNotFoundException;
import com.mylearning.userservice.repository.UserRepository;
import com.mylearning.userservice.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDto register(UserRequestDto requestDto) {
        User user = UserMapper.toUser(requestDto);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return UserMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getProfileByUsername(String username) {
        return UserMapper.toUserResponseDto(userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    @Override
    public UserResponseDto getProfileByEmail(String email) {
        return UserMapper.toUserResponseDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found")));
    }
}
