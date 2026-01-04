package dev.kingscode.ecommerce_api.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import dev.kingscode.ecommerce_api.dto.email.VerificationTokenRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.CreateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.LoginRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.RefreshTokenRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.ResetPasswordRequestDto;
import dev.kingscode.ecommerce_api.dto.user.response.LoginResponseDto;
import dev.kingscode.ecommerce_api.dto.user.response.RefreshTokenResponseDto;
import dev.kingscode.ecommerce_api.dto.user.response.UserResponseDto;
import dev.kingscode.ecommerce_api.exception.AuthorizationException;
import dev.kingscode.ecommerce_api.service.AuthService;
import dev.kingscode.ecommerce_api.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Value("${app.client-base-url}")
    private String frontendUrl;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto signup(@Valid @RequestBody CreateUserRequestDto createUserRequestDto) {
        return authService.signup(createUserRequestDto, false);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Login attempt for email: {}", loginRequestDto.getEmail());

        LoginResponseDto response = authService.authenticate(loginRequestDto);

        log.info("Login successful for email: {}", loginRequestDto.getEmail());

        return response;
    }

    @GetMapping("/verify-email")
    public RedirectView verifyUserEmail(@NotBlank @RequestParam String token) {
        try {
            userService.verifyEmail(token);
            String frontendLoginUrl = frontendUrl + "/auth/login?verified=true&message=Email+verified+successfully";

            return new RedirectView(frontendLoginUrl);
        } catch (AuthorizationException ex) {
            String errorCode = ex.getErrorCode().getCode();
            String errorMessage = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
            String frontendLoginUrl = frontendUrl + "/auth/login?error=true&errorCode=" + errorCode + "&message="
                    + errorMessage;

            return new RedirectView(frontendLoginUrl);
        }

    }

    @PostMapping("/resend-verification")
    public void resendVerificationEmail(@Valid @RequestBody VerificationTokenRequestDto requestDto) {
        userService.resendUserVerificationEmail(requestDto.getEmail());

    }

    @PostMapping("/forgot-password")
    public void sendPasswordResetToken(@Valid @RequestBody VerificationTokenRequestDto requestDto) {
        authService.sendPasswordResetTokenEmail(requestDto.getEmail());

    }

    @PostMapping("/reset-password")
    public void updateUserPassword(@Valid @RequestBody ResetPasswordRequestDto requestDto) {
        userService.updateUserPassword(requestDto);

    }

    @PostMapping("/refresh-token")
    public RefreshTokenResponseDto refreshAccessToken(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        return authService.refreshAccessToken(requestDto.getRefreshToken());

    }

}
