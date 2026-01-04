package dev.kingscode.ecommerce_api.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.kingscode.ecommerce_api.dto.user.request.UpdateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.response.UserResponseDto;
import dev.kingscode.ecommerce_api.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getUserById(@PathVariable String id) {
        UUID userId = UUID.fromString(id);
        return userService.getUserById(userId);
    }

    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserRequestDto dto) {
        UUID userId = UUID.fromString(id);

        return userService.updateUser(userId, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        UUID userId = UUID.fromString(id);
        userService.deleteUser(userId);
    }

}
