package dev.kingscode.ecommerce_api.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kingscode.ecommerce_api.dto.user.request.CreateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.LoginRequestDto;
import dev.kingscode.ecommerce_api.dto.user.response.LoginResponseDto;
import dev.kingscode.ecommerce_api.dto.user.response.RefreshTokenResponseDto;
import dev.kingscode.ecommerce_api.dto.user.response.UserResponseDto;
import dev.kingscode.ecommerce_api.exception.AuthenticationException;
import dev.kingscode.ecommerce_api.exception.AuthorizationException;
import dev.kingscode.ecommerce_api.exception.EmailNotVerifiedException;
import dev.kingscode.ecommerce_api.exception.ErrorCode;
import dev.kingscode.ecommerce_api.mapper.UserMapper;
import dev.kingscode.ecommerce_api.model.User;
import dev.kingscode.ecommerce_api.repository.UserRepository;
import dev.kingscode.ecommerce_api.service.email.VerificationEmailService;
import dev.kingscode.ecommerce_api.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final VerificationEmailService emailService;
    private final TokenGenerator tokenGenerator;
    private final UserRepository userRepo;

    /** Register a new user */
    public UserResponseDto signup(CreateUserRequestDto requestDto, Boolean isAdmin) {
        User user = userService.createUser(requestDto, isAdmin);

        String verificationToken = tokenGenerator.generateToken();
        Instant tokenExpiration = Instant.now().plus(24, ChronoUnit.HOURS);

        User updatedUser = user.toBuilder().verificationToken(verificationToken).tokenExpiration(tokenExpiration)
                .build();

        // Send verification email
        emailService.sendUserVerificationEmail(requestDto.getEmail(), requestDto.getFirstName(),
                verificationToken, tokenExpiration);

        userRepo.save(updatedUser);

        return userMapper.toResponseDto(user);
    }

    /**
     * Authenticates user and returns generated JWT and RefreshToken
     * 
     * @return
     */
    public LoginResponseDto authenticate(LoginRequestDto loginRequest) {
        try {

            User user = userService.findByEmail(loginRequest.getEmail());

            // Check if user account is verified
            if (!user.isVerified()) {
                log.warn("Login attempt with unverified email: {}", loginRequest.getEmail());

                // refresh token if expired
                user = userService.refreshVerificationTokenIfExpired(user);

                emailService.sendUserVerificationEmail(user.getEmail(), user.getFirstName(),
                        user.getVerificationToken(), user.getTokenExpiration());

                throw new EmailNotVerifiedException(
                        "Please verify your email address to continue",
                        ErrorCode.EMAIL_NOT_VERIFIED,
                        user.getEmail());
            }

            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            // load user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate JWT access and refresh token
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return LoginResponseDto.builder().accessToken(accessToken).tokenType("Bearer").refreshToken(refreshToken)
                    .fullAuthenticated(true)
                    .user(LoginResponseDto.UserResponse.builder().id(user.getId()).email(user.getEmail())
                            .firstName(user.getFirstName()).lastName(user.getLastName()).role(user.getRole()).build())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for email: {}", loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        } catch (UsernameNotFoundException e) {
            log.warn("User not found for email: {}", loginRequest.getEmail());
            throw new AuthenticationException("User not found");
        } catch (EmailNotVerifiedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            throw new AuthenticationException("Authentication failed");
        }
    }

    /** Send password reset token to users email */
    public void sendPasswordResetTokenEmail(String email) {
        User user = userService.findByEmail(email);

        Instant now = Instant.now();

        String newOtp = tokenGenerator.generateDigitOtp();
        Instant tokenExpiration = now.plus(15, ChronoUnit.MINUTES);

        User updatedUser = user.toBuilder().pwdResetToken(newOtp).tokenExpiration(tokenExpiration).build();

        userRepo.save(updatedUser);

        emailService.sendPasswordResetEmail(email, updatedUser.getFirstName(), newOtp, tokenExpiration);

    }

    public RefreshTokenResponseDto refreshAccessToken(String refreshToken) {

        String email;

        // Extract email from refresh token

        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new AuthorizationException("Invalid refresh token", ErrorCode.INVALID_TOKEN);
        }

        User user = userService.findByEmail(email);

        // validate refresh token

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new AuthorizationException("Refresh token expired or invalid", ErrorCode.TOKEN_EXPIRED);
        }

        String newAccessToken = jwtService.generateToken(user);

        return RefreshTokenResponseDto.builder().accessToken(newAccessToken).build();
    }

}
