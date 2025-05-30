package com.mylearning.userservice.controller;

import com.mylearning.userservice.dto.UserRequestDto;
import com.mylearning.userservice.dto.UserResponseDto;
import com.mylearning.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @GetMapping("/profile/username/{username}")
    public ResponseEntity<UserResponseDto> getProfileByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getProfileByUsername(username));
    }

    @GetMapping("/profile/email/{email}")
    public ResponseEntity<UserResponseDto> getProfileByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getProfileByEmail(email));
    }
}
