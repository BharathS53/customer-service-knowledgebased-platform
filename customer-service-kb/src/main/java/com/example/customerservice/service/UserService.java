package com.example.customerservice.service;

import com.example.customerservice.dto.RegisterRequest;
import com.example.customerservice.dto.UserDto;
import com.example.customerservice.entity.User;

import java.util.List;

public interface UserService {
    UserDto registerUser(RegisterRequest request);
    UserDto getUserById(Long userId);
    UserDto getUserByUsername(String username);
    User getUserEntityById(Long userId);
    User getUserEntityByUsername(String username);
    List<UserDto> getAllUsers();
    List<UserDto> getAllAgents();
    List<UserDto> getAllCustomers();
    UserDto toggleUserStatus(Long userId);
    UserDto updateUser(Long userId, UserDto userDto);
    void deleteUser(Long userId);
}
