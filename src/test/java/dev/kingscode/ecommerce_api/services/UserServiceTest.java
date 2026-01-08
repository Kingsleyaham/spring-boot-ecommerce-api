package dev.kingscode.ecommerce_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import dev.kingscode.ecommerce_api.dto.FileUploadResult;
import dev.kingscode.ecommerce_api.dto.user.request.CreateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.UpdateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.response.UserResponseDto;
import dev.kingscode.ecommerce_api.exception.AuthorizationException;
import dev.kingscode.ecommerce_api.exception.ErrorCode;
import dev.kingscode.ecommerce_api.exception.ResourceConflictException;
import dev.kingscode.ecommerce_api.exception.ResourceNotFoundException;
import dev.kingscode.ecommerce_api.mapper.UserMapper;
import dev.kingscode.ecommerce_api.model.User;
import dev.kingscode.ecommerce_api.model.enums.UserRole;
import dev.kingscode.ecommerce_api.repository.UserRepository;
import dev.kingscode.ecommerce_api.service.UserService;
import dev.kingscode.ecommerce_api.service.email.VerificationEmailService;
import dev.kingscode.ecommerce_api.service.storage.FileStorageService;
import dev.kingscode.ecommerce_api.service.storage.FileValidationService;
import dev.kingscode.ecommerce_api.util.TokenGenerator;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
        @Mock
        private UserRepository userRepo;
        @Mock
        private UserMapper userMapper;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private TokenGenerator tokenGenerator;
        @Mock
        private VerificationEmailService emailService;
        @Mock
        private FileValidationService fileValidationService;
        @Mock
        private FileStorageService fileStorageService;

        @InjectMocks
        private UserService userService;

        private UUID testUserId;
        private User testUser;
        private UserResponseDto testUserResponseDto;
        private CreateUserRequestDto createUserRequestDto;

        @BeforeEach
        void setup() {
                testUserId = UUID.randomUUID();

                testUser = User.builder()
                                .id(testUserId)
                                .email("test@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .role(UserRole.USER)
                                .password("encodedPassword123")
                                .emailVerified(false)
                                .verificationToken("verification-token")
                                .tokenExpiration(Instant.now().plus(24, ChronoUnit.HOURS))
                                .profileImage("https://storage.example.com/avatar.jpg")
                                .build();

                testUserResponseDto = UserResponseDto.builder()
                                .id(testUserId)
                                .email("test@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .role(UserRole.USER)
                                .emailVerified(false)
                                .profileImage("https://storage.example.com/avatar.jpg")
                                .build();

                createUserRequestDto = CreateUserRequestDto.builder()
                                .email("newuser@example.com")
                                .password("password123")
                                .firstName("Jane")
                                .lastName("Smith")
                                .build();

        }

        @Test
        void getUserById_ValidId_ReturnsUserResponseDto() {
                when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
                when(userMapper.toResponseDto(testUser)).thenReturn(testUserResponseDto);

                UserResponseDto result = userService.getUserById(testUserId);

                // Assertions
                assertNotNull(result);
                assertEquals(testUserId, result.getId());
                assertEquals("test@example.com", result.getEmail());
                assertEquals("John", result.getFirstName());

                verify(userRepo).findById(testUserId);
                verify(userMapper).toResponseDto(testUser);
        }

        @Test
        void getUserById_InvalidId_throwsResourceNotFound() {
                UUID invalidId = UUID.randomUUID();
                when(userRepo.findById(invalidId)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                                () -> userService.getUserById(invalidId));

                // verify exception message format
                assertEquals("User not found with id: '" + invalidId + "'", exception.getMessage());
                assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());

                verify(userRepo).findById(invalidId);
                verify(userMapper, never()).toResponseDto(any()); // verify that the userMapper.toResponseDto was never
                                                                  // called

        }

        @Test
        void findByEmail_ValidEmail_ReturnsUser() {
                String email = "test@example.com";
                when(userRepo.findByEmail(email)).thenReturn(Optional.of(testUser));

                User result = userService.findByEmail(email);

                // Assertions
                assertNotNull(result);
                assertEquals(email, result.getEmail());

                verify(userRepo).findByEmail(email);
        }

        @Test
        void findByEmail_InvalidEmail_ThrowsResourceNotFoundException() {
                String invalidEmail = "invalid@example.com";
                when(userRepo.findByEmail(invalidEmail)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                                () -> userService.findByEmail(invalidEmail));

                // Assertions
                assertEquals("User not found with email: 'invalid@example.com'", exception.getMessage());
                assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());

                verify(userRepo).findByEmail(invalidEmail);
        }

        @Test
        void createUser_NewUser_CreateSuccessful() {
                boolean isAdmin = false;
                String encodedPassword = "encodedPassword123";
                User newUser = User.builder()
                                .email(createUserRequestDto.getEmail())
                                .firstName(createUserRequestDto.getFirstName())
                                .lastName(createUserRequestDto.getLastName())
                                .password(encodedPassword)
                                .role(UserRole.USER)
                                .build();

                User savedUser = newUser.toBuilder().id(UUID.randomUUID()).build();

                when(userRepo.existsByEmail(createUserRequestDto.getEmail())).thenReturn(false);
                when(userMapper.toEntity(createUserRequestDto)).thenReturn(newUser);
                when(userRepo.save(any(User.class))).thenReturn(savedUser);

                User result = userService.createUser(createUserRequestDto, isAdmin);

                // Assertions
                assertNotNull(result);
                assertEquals(savedUser.getId(), result.getId());
                assertEquals(UserRole.USER, result.getRole());

                verify(userRepo).existsByEmail(createUserRequestDto.getEmail());
                verify(passwordEncoder).encode(createUserRequestDto.getPassword());
                verify(userMapper).toEntity(createUserRequestDto);
                verify(userRepo).save(any(User.class));
        }

        @Test
        void createUser_DuplicateEmail_ThrowsResourceConflictException() {
                when(userRepo.existsByEmail(createUserRequestDto.getEmail())).thenReturn(true);

                ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                                () -> userService.createUser(createUserRequestDto, false));

                assertEquals("User already exist", exception.getMessage());
                assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.getErrorCode());

                verify(userRepo).existsByEmail(createUserRequestDto.getEmail());
                verify(passwordEncoder, never()).encode(anyString());
                verify(userRepo, never()).save(any(User.class));
        }

        @Test
        void updateUser_ValidRequest_UpdateSuccessfully() {
                UpdateUserRequestDto updateDto = UpdateUserRequestDto.builder()
                                .firstName("UpdatedFirstName")
                                .lastName("UpdatedLastName")
                                .build();

                User updatedUser = testUser.toBuilder()
                                .firstName("UpdatedFirstName")
                                .lastName("UpdatedLastName")
                                .build();

                UserResponseDto updatedResponse = testUserResponseDto.toBuilder()
                                .firstName("UpdatedFirstName")
                                .lastName("UpdatedLastName")
                                .build();

                when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
                when(userRepo.save(any(User.class))).thenReturn(updatedUser);
                when(userMapper.toResponseDto(updatedUser)).thenReturn(updatedResponse);

                UserResponseDto result = userService.updateUser(testUserId, updateDto);

                // assertions
                assertNotNull(result);
                assertEquals("UpdatedFirstName", result.getFirstName());
                assertEquals("UpdatedLastName", result.getLastName());

                verify(userRepo).findById(testUserId);
                verify(userRepo).save(any(User.class));
                verify(userMapper).toResponseDto(updatedUser);
        }

        @Test
        void updateUser_InvalidUserId_ThrowsResourceNotFoundException() {
                UUID invalidUserId = UUID.randomUUID();
                UpdateUserRequestDto updateDto = UpdateUserRequestDto.builder()
                                .firstName("UpdatedName")
                                .build();

                when(userRepo.findById(invalidUserId)).thenReturn(Optional.empty());

                // Act & Assert
                ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                                () -> userService.updateUser(invalidUserId, updateDto));

                assertEquals("User not found with id: " + invalidUserId, exception.getMessage());

                verify(userRepo).findById(invalidUserId);
                verify(userRepo, never()).save(any(User.class));
        }

        @Test
        void deleteUser_ValidId_DeletesSuccessfully() {
                when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));

                userService.deleteUser(testUserId);

                // Assertion
                verify(userRepo).findById(testUserId);
                verify(userRepo).delete(testUser);
        }

        @Test
        void deleteUser_InvalidId_ThrowsResourceNotFoundException() {
                UUID invalidId = UUID.randomUUID();
                when(userRepo.findById(invalidId)).thenReturn(Optional.empty());

                // Act & Assertions
                ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                                () -> userService.deleteUser(invalidId));

                assertEquals("User not found with id: " + invalidId, exception.getMessage());

                verify(userRepo).findById(invalidId);
                verify(userRepo, never()).delete(any());
        }

        @Test
        void uploadProfileImage_ValidFile_UploadSuccessfully() {
                MultipartFile mockFile = mock(MultipartFile.class);
                FileUploadResult uploadResult = FileUploadResult.builder()
                                .url("https://storage.example.com/new-avatar.jpg")
                                .fileKey("new-avatar")
                                .fileName("new-avatar-jpg")
                                .size(1024L)
                                .build();

                User updateUser = testUser.toBuilder().profileImage("https://storage.example.com/new-avatar.jpg")
                                .build();

                when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
                doNothing().when(fileValidationService).validateImage(mockFile);
                when(fileStorageService.uploadFile(mockFile, "avatars")).thenReturn(uploadResult);
                when(userRepo.save(any(User.class))).thenReturn(updateUser);

                FileUploadResult result = userService.uploadProfileImage(testUserId, mockFile);

                assertNotNull(result);
                assertEquals("https://storage.example.com/new-avatar.jpg", result.getUrl());

                verify(fileValidationService).validateImage(mockFile);
                verify(fileStorageService).uploadFile(mockFile, "avatars");
                verify(userRepo).save(any(User.class));
        }

        @Test
        void verifyEmail_ValidToken_VerifiesSuccessfully() {
                String validToken = "valid-token";
                User unverifiedUser = testUser.toBuilder()
                                .emailVerified(false)
                                .verificationToken(validToken)
                                .tokenExpiration(Instant.now().plus(1, ChronoUnit.HOURS))
                                .build();

                User verifiedUser = unverifiedUser.toBuilder()
                                .emailVerified(true)
                                .verificationToken(null)
                                .tokenExpiration(null)
                                .build();

                when(userRepo.findByVerificationToken(validToken)).thenReturn(Optional.of(unverifiedUser));
                when(userRepo.save(any(User.class))).thenReturn(verifiedUser);

                userService.verifyEmail(validToken);

                verify(userRepo).findByVerificationToken(validToken);
                verify(userRepo).save(any(User.class));

        }

        @Test
        void verifyEmail_InvalidToken_ThrowsAuthorizationException() {
                String invalidToken = "invalid-token";
                when(userRepo.findByVerificationToken(invalidToken)).thenReturn(Optional.empty());

                AuthorizationException exception = assertThrows(AuthorizationException.class,
                                () -> userService.verifyEmail(invalidToken));

                assertEquals("Invalid or already used verification token", exception.getMessage());
                assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());

                verify(userRepo).findByVerificationToken(invalidToken);
                verify(userRepo, never()).save(any());
        }

        @Test
        void verifyEmail_ExpiredToken_ThrowsAuthorizationException() {
                String expiredToken = "expired-token";
                User user = testUser.toBuilder()
                                .verificationToken(expiredToken)
                                .tokenExpiration(Instant.now().minus(1, ChronoUnit.HOURS))
                                .build();

                when(userRepo.findByVerificationToken(expiredToken)).thenReturn(Optional.of(user));

                AuthorizationException exception = assertThrows(AuthorizationException.class,
                                () -> userService.verifyEmail(expiredToken));

                assertEquals(ErrorCode.TOKEN_EXPIRED, exception.getErrorCode());

                verify(userRepo, never()).save(any());
        }

        @Test
        void verifyEmail_AlreadyVerified_ThrowsAuthorizationException() {
                String token = "already-verified-token";
                User verifiedUser = testUser.toBuilder()
                                .emailVerified(true)
                                .verificationToken(token)
                                .tokenExpiration(Instant.now().plus(1, ChronoUnit.HOURS))
                                .build();

                when(userRepo.findByVerificationToken(token)).thenReturn(Optional.of(verifiedUser));

                AuthorizationException exception = assertThrows(AuthorizationException.class,
                                () -> userService.verifyEmail(token));

                assertEquals(ErrorCode.EMAIL_ALREADY_VERIFIED, exception.getErrorCode());

                verify(userRepo, never()).save(any());
        }

        @Test
        void resendUserVerificationEmail_AlreadyVerified_ThrowsAuthorizationException() {
                // Arrange
                String email = "verified@example.com";
                User verifiedUser = testUser.toBuilder()
                                .email(email)
                                .emailVerified(true)
                                .build();

                when(userRepo.findByEmail(email)).thenReturn(Optional.of(verifiedUser));

                // Act & Assert
                AuthorizationException exception = assertThrows(AuthorizationException.class,
                                () -> userService.resendUserVerificationEmail(email));

                assertEquals(ErrorCode.EMAIL_ALREADY_VERIFIED, exception.getErrorCode());

                verify(emailService, never()).sendUserVerificationEmail(any(), any(), any(), any());
        }

}
