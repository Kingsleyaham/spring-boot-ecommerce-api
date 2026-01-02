package dev.kingscode.ecommerce_api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kingscode.ecommerce_api.dto.user.request.CreateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.UpdateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.response.UserResponseDto;
import dev.kingscode.ecommerce_api.exception.ResourceConflictException;
import dev.kingscode.ecommerce_api.exception.ResourceNotFoundException;
import dev.kingscode.ecommerce_api.mapper.UserMapper;
import dev.kingscode.ecommerce_api.model.User;
import dev.kingscode.ecommerce_api.model.enums.UserRole;
import dev.kingscode.ecommerce_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID userId) {
        @SuppressWarnings("null")
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return userMapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return user;
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("updatedAt").descending());

        return userRepo.findAll(pageable).map(userMapper::toResponseDto);

    }

    public UserResponseDto createUser(CreateUserRequestDto dto, boolean isAdmin) {

        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new ResourceConflictException("User already exist");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword()); // hashed password
        User user = userMapper.toEntity(dto).toBuilder().password(encodedPassword)
                .role(isAdmin ? UserRole.ADMIN : UserRole.USER).build();

        @SuppressWarnings("null")
        User savedUser = userRepo.save(user);

        return userMapper.toResponseDto(savedUser);
    }

    public UserResponseDto updateUser(UUID userId, UpdateUserRequestDto dto) {

        User existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        User updatedUser = existingUser.toBuilder()
                .firstName(dto.getFirstName() != null ? dto.getFirstName() : existingUser.getFirstName())
                .lastName(dto.getLastName() != null ? dto.getLastName() : existingUser.getLastName()).build();

        userRepo.save(updatedUser);

        return userMapper.toResponseDto(updatedUser);

    }

    public void deleteUser(UUID userId) {
        User existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepo.delete(existingUser);
    }

}
