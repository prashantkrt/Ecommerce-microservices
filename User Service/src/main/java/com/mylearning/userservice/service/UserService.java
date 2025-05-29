package com.mylearning.userservice.service;

import com.mylearning.userservice.dto.UserRequestDto;
import com.mylearning.userservice.dto.UserResponseDto;
import com.mylearning.userservice.entity.User;

public interface UserService {
    public UserResponseDto register(UserRequestDto user);
    public UserResponseDto getProfileByUsername(String username);
    public UserResponseDto getProfileByEmail(String email);
}
