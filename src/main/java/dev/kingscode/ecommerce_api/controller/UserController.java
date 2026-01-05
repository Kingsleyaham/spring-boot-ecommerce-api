package dev.kingscode.ecommerce_api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.kingscode.ecommerce_api.dto.user.request.UpdateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.response.UserResponseDto;
import dev.kingscode.ecommerce_api.mapper.UserMapper;
import dev.kingscode.ecommerce_api.model.User;
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
    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserResponseDto getAuthenticatedUser(@AuthenticationPrincipal User user) {
        return userMapper.toResponseDto(user);
    }

    @PutMapping("/me/update")
    public UserResponseDto updateUser(@AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserRequestDto dto) {

        return userService.updateUser(user.getId(), dto);
    }

}
