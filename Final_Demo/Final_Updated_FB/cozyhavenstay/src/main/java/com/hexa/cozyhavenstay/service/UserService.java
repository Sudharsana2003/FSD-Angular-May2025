// src/main/java/com/hexa/cozyhavenstay/service/UserService.java
package com.hexa.cozyhavenstay.service;

import com.hexa.cozyhavenstay.dto.UserDto;
import com.hexa.cozyhavenstay.dto.UserResponseDto;
import com.hexa.cozyhavenstay.model.User;

import java.util.List;

public interface UserService {
    UserResponseDto registerUser(UserDto userDto);
    UserResponseDto getUserById(Integer userId);
    List<UserResponseDto> getAllUsers();
    UserResponseDto updateUser(Integer userId, UserDto userDto);
    boolean deleteUser(Integer userId);

    // NEW: Method to get the currently authenticated user's profile
    UserResponseDto getCurrentUserProfile(String username); // We'll pass the username from security context
}