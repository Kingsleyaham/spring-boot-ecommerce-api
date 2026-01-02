package dev.kingscode.ecommerce_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.kingscode.ecommerce_api.dto.user.request.CreateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.LoginRequestDto;
import dev.kingscode.ecommerce_api.dto.user.response.LoginResponseDto;
import dev.kingscode.ecommerce_api.dto.user.response.UserResponseDto;
import dev.kingscode.ecommerce_api.service.AuthService;
import dev.kingscode.ecommerce_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto signup(@Valid @RequestBody CreateUserRequestDto createUserRequestDto) {
        return userService.createUser(createUserRequestDto, false);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Login attempt for email: {}", loginRequestDto.getEmail());

        LoginResponseDto response = authService.authenticate(loginRequestDto);

        log.info("Login successful for email: {}", loginRequestDto.getEmail());

        return response;
    }

    @PostMapping("/token/refresh")
    public String refreshAccessToken(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

}
