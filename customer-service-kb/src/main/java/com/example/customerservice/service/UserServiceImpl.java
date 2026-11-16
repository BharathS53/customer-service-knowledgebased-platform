package com.example.customerservice.service;

import com.example.customerservice.dto.RegisterRequest;
import com.example.customerservice.dto.UserDto;
import com.example.customerservice.entity.Role;
import com.example.customerservice.entity.RoleType;
import com.example.customerservice.entity.User;
import com.example.customerservice.exception.BadRequestException;
import com.example.customerservice.exception.ResourceNotFoundException;
import com.example.customerservice.repository.RoleRepository;
import com.example.customerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        final RoleType roleType;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                roleType = RoleType.valueOf(request.getRole().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role specified: " + request.getRole());
            }
        } else {
            roleType = RoleType.ROLE_CUSTOMER;
        }

        Role assignedRole = roleRepository.findByName(roleType)
                .orElseThrow(() -> new ResourceNotFoundException("Role " + roleType + " not found in database"));

        User user = User.builder()
                .username(request.getUsername().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .roles(new HashSet<>(Collections.singletonList(assignedRole)))
                .build();

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        return mapToDto(getUserEntityById(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        return mapToDto(getUserEntityByUsername(username));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllAgents() {
        return userRepository.findByRoles_Name(RoleType.ROLE_AGENT).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllCustomers() {
        return userRepository.findByRoles_Name(RoleType.ROLE_CUSTOMER).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto toggleUserStatus(Long userId) {
        User user = getUserEntityById(userId);
        user.setIsActive(!user.getIsActive());
        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = getUserEntityById(userId);
        if (userDto.getFullName() != null && !userDto.getFullName().isBlank()) {
            user.setFullName(userDto.getFullName().trim());
        }
        if (userDto.getPhoneNumber() != null) {
            user.setPhoneNumber(userDto.getPhoneNumber().trim());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail().trim().toLowerCase())) {
                throw new BadRequestException("Email '" + userDto.getEmail() + "' is already in use");
            }
            user.setEmail(userDto.getEmail().trim().toLowerCase());
        }
        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = getUserEntityById(userId);
        userRepository.delete(user);
    }

    private UserDto mapToDto(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
