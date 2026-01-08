package dev.kingscode.ecommerce_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
import dev.kingscode.ecommerce_api.model.enums.UserRole;
import dev.kingscode.ecommerce_api.repository.UserRepository;
import dev.kingscode.ecommerce_api.service.AuthService;
import dev.kingscode.ecommerce_api.service.JWTService;
import dev.kingscode.ecommerce_api.service.UserService;
import dev.kingscode.ecommerce_api.service.email.VerificationEmailService;
import dev.kingscode.ecommerce_api.util.TokenGenerator;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserService userService;
    @Mock
    private JWTService jwtService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private UserMapper userMapper;
    @Mock
    private VerificationEmailService emailService;
    @Mock
    private TokenGenerator tokenGenerator;

    @InjectMocks
    private AuthService authService;

    private UUID testUserId;
    private User testUser;
    private LoginRequestDto loginRequestDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .role(UserRole.USER)
                .emailVerified(true)
                .build();

        // Create login request

        loginRequestDto = LoginRequestDto.builder()
                .email("test@example.com")
                .password("password123")
                .build();

    }

    @Test
    void signup_ValidRequest_CreatesUserAndSendsVerificationEmail() {
        boolean isAdmin = false;
        String generatedToken = "verify-token-123";
        Instant tokenExpiration = Instant.now().plus(24,
                ChronoUnit.HOURS);

        CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
                .email("new@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        User createdUser = User.builder()
                .id(UUID.randomUUID())
                .email(requestDto.getEmail())
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .role(UserRole.USER)
                .emailVerified(false)
                .verificationToken(generatedToken)
                .tokenExpiration(null)
                .tokenExpiration(tokenExpiration)
                .build();

        UserResponseDto mappedResponse = UserResponseDto.builder()
                .id(createdUser.getId())
                .email(createdUser.getEmail())
                .firstName(createdUser.getFirstName())
                .lastName(createdUser.getLastName())
                .role(createdUser.getRole())
                .emailVerified(createdUser.isVerified())
                .build();

        when(userService.createUser(requestDto, isAdmin)).thenReturn(createdUser);
        when(tokenGenerator.generateToken()).thenReturn(generatedToken);
        doNothing().when(emailService).sendUserVerificationEmail(
                eq(requestDto.getEmail()),
                eq(requestDto.getFirstName()),
                eq(generatedToken),
                any(Instant.class));
        when(userMapper.toResponseDto(createdUser)).thenReturn(mappedResponse);

        UserResponseDto result = authService.signup(requestDto, false);

        assertNotNull(result);
        assertFalse(result.isEmailVerified());
        assertEquals(mappedResponse, result);

        verify(userService).createUser(requestDto, isAdmin);
        verify(tokenGenerator).generateToken();
        verify(emailService).sendUserVerificationEmail(
                eq(requestDto.getEmail()),
                eq(requestDto.getFirstName()),
                eq(generatedToken),
                any(Instant.class));
        verify(userRepo).save(any(User.class));
        verify(userMapper).toResponseDto(createdUser);

    }

    @Test
    void authenticate_ValidCredentialsAndVerifiedUser_ReturnsLoginResponse() {
        User verifiedUser = testUser.toBuilder()
                .emailVerified(true)
                .build();

        Authentication mockAuthentication = mock(Authentication.class);
        UserDetails mockUserDetails = mock(UserDetails.class);

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        LoginResponseDto expectedResponse = LoginResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .refreshToken(refreshToken)
                .fullAuthenticated(true)
                .user(LoginResponseDto.UserResponse.builder()
                        .id(testUserId)
                        .email("test@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .role(UserRole.USER)
                        .build())
                .build();

        when(userService.findByEmail("test@example.com")).thenReturn(verifiedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(mockUserDetails)).thenReturn(refreshToken);

        // Act
        LoginResponseDto result = authService.authenticate(loginRequestDto);

        // Assertions
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertNotNull(result.getUser());
        assertEquals("test@example.com", result.getUser().getEmail());
        assertTrue(result.getFullAuthenticated());

        verify(userService).findByEmail("test@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(mockUserDetails);
        verify(jwtService).generateRefreshToken(mockUserDetails);
    }

    @Test
    void authenticate_UnverifiedUser_ThrowsEmailNotVerifiedException() {
        User unverifiedUser = testUser.toBuilder()
                .emailVerified(false)
                .verificationToken("old-token")
                .tokenExpiration(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        User refreshedUser = unverifiedUser.toBuilder()
                .verificationToken("new-token")
                .tokenExpiration(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        when(userService.findByEmail("test@example.com")).thenReturn(unverifiedUser);
        when(userService.refreshVerificationTokenIfExpired(unverifiedUser)).thenReturn(refreshedUser);
        doNothing().when(emailService).sendUserVerificationEmail(
                eq("test@example.com"),
                eq("John"),
                eq("new-token"),
                any(Instant.class));

        // Action & Assertions
        EmailNotVerifiedException exception = assertThrows(EmailNotVerifiedException.class,
                () -> authService.authenticate(loginRequestDto));

        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, exception.getErrorCode());
        verify(userService).findByEmail("test@example.com");
        verify(userService).refreshVerificationTokenIfExpired(unverifiedUser);
        verify(emailService).sendUserVerificationEmail(
                eq("test@example.com"),
                eq("John"),
                eq("new-token"),
                any(Instant.class));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_BadCredentials_ThrowsAuthenticationException() {
        User verifiedUser = testUser.toBuilder()
                .emailVerified(true)
                .build();

        when(userService.findByEmail("test@example.com")).thenReturn(verifiedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authService.authenticate(loginRequestDto));

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userService).findByEmail("test@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_UserNotFound_ThrowsAuthenticationException() {
        String invalidEmail = "nonexistent@example.com";

        when(userService.findByEmail(invalidEmail))
                .thenThrow(new UsernameNotFoundException("User not found"));

        LoginRequestDto invalidLogin = LoginRequestDto.builder()
                .email(invalidEmail)
                .password("password123")
                .build();

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authService.authenticate(invalidLogin));

        assertEquals("User not found", exception.getMessage());

        verify(userService).findByEmail(invalidEmail);
        verify(authenticationManager, never()).authenticate(any());

    }

    @Test
    void sendPasswordResetTokenEmail_ValidEmail_SendsResetEmail() {
        String email = "user@example.com";
        String otp = "123456";
        Instant tokenExpiration = Instant.now().plus(15, ChronoUnit.MINUTES);

        User user = testUser.toBuilder()
                .email(email)
                .firstName("John")
                .build();

        User updatedUser = user.toBuilder()
                .pwdResetToken(otp)
                .tokenExpiration(tokenExpiration)
                .build();

        when(userService.findByEmail(email)).thenReturn(user);
        when(tokenGenerator.generateDigitOtp()).thenReturn(otp);
        when(userRepo.save(any(User.class))).thenReturn(updatedUser);
        doNothing().when(emailService).sendPasswordResetEmail(
                eq(email),
                eq("John"),
                eq(otp),
                any(Instant.class));

        authService.sendPasswordResetTokenEmail(email);

        verify(userService).findByEmail(email);
        verify(tokenGenerator).generateDigitOtp();
        verify(userRepo).save(any(User.class));
        verify(emailService).sendPasswordResetEmail(
                eq(email),
                eq("John"),
                eq(otp),
                any(Instant.class));
    }

    @Test
    void sendPasswordResetTokenEmail_UserNotFound_ThrowsException() {
        String email = "nonexistent@example.com";
        when(userService.findByEmail(email))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> authService.sendPasswordResetTokenEmail(email));

        verify(userService).findByEmail(email);
        verify(tokenGenerator, never()).generateDigitOtp();
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any(), any());
    }

    @Test
    void refreshAccessToken_ValidRefreshToken_ReturnsNewAccessToken() {
        String refreshToken = "valid-refresh-token";
        String email = "test@example.com";
        String newAccessToken = "new-access-token";

        User user = testUser.toBuilder()
                .email(email)
                .build();

        RefreshTokenResponseDto expectedResponse = RefreshTokenResponseDto.builder()
                .accessToken(newAccessToken)
                .build();

        when(jwtService.extractUsername(refreshToken)).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(user);
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(newAccessToken);

        // Act
        RefreshTokenResponseDto result = authService.refreshAccessToken(refreshToken);

        // Assertions
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        verify(jwtService).extractUsername(refreshToken);
        verify(userService).findByEmail(email);
        verify(jwtService).isTokenValid(refreshToken, user);
        verify(jwtService).generateToken(user);
    }

    @Test
    void refreshAccessToken_ExpiredRefreshToken_ThrowsAuthorizationException() {
        String expiredRefreshToken = "expired-refresh-token";
        String email = "test@example.com";

        User user = testUser.toBuilder()
                .email(email)
                .build();

        when(jwtService.extractUsername(expiredRefreshToken)).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(user);
        when(jwtService.isTokenValid(expiredRefreshToken, user)).thenReturn(false);

        // Actions & Assertions
        AuthorizationException exception = assertThrows(AuthorizationException.class,
                () -> authService.refreshAccessToken(expiredRefreshToken));

        assertEquals(ErrorCode.TOKEN_EXPIRED, exception.getErrorCode());

        verify(jwtService).extractUsername(expiredRefreshToken);
        verify(userService).findByEmail(email);
        verify(jwtService).isTokenValid(expiredRefreshToken, user);
        verify(jwtService, never()).generateToken(any());
    }

}
