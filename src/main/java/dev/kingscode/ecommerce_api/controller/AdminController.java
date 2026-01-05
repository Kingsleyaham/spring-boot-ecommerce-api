package dev.kingscode.ecommerce_api.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.kingscode.ecommerce_api.dto.user.response.UserResponseDto;
import dev.kingscode.ecommerce_api.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final UserService userService;

    @GetMapping("/users")
    public Page<UserResponseDto> getUsers(@RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        return userService.getUsers(page, size);

    }

    @GetMapping("/users/{id}")
    public UserResponseDto getUserById(@PathVariable String id) {
        UUID userId = UUID.fromString(id);
        return userService.getUserById(userId);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable String id) {
        UUID userId = UUID.fromString(id);
        userService.deleteUser(userId);
    }

}
