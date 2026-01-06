package dev.kingscode.ecommerce_api.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dev.kingscode.ecommerce_api.dto.FileUploadResult;
import dev.kingscode.ecommerce_api.dto.user.request.CreateUserRequestDto;
import dev.kingscode.ecommerce_api.dto.user.request.ResetPasswordRequestDto;
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
import dev.kingscode.ecommerce_api.service.email.VerificationEmailService;
import dev.kingscode.ecommerce_api.service.storage.FileStorageService;
import dev.kingscode.ecommerce_api.service.storage.FileValidationService;
import dev.kingscode.ecommerce_api.util.TokenGenerator;
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
    private final TokenGenerator tokenGenerator;
    private final VerificationEmailService emailService;
    private final FileValidationService fileValidationService;
    private final FileStorageService fileStorageService;

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

    public User createUser(CreateUserRequestDto dto, boolean isAdmin) {

        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new ResourceConflictException("User already exist");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword()); // hashed password
        User user = userMapper.toEntity(dto).toBuilder().password(encodedPassword)
                .role(isAdmin ? UserRole.ADMIN : UserRole.USER).build();

        @SuppressWarnings("null")
        User savedUser = userRepo.save(user);

        return savedUser;
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

    public FileUploadResult uploadProfileImage(UUID userId, MultipartFile file) {
        User existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // validate uploaded file
        fileValidationService.validateImage(file);

        // upload file
        FileUploadResult uploadedFile = fileStorageService.uploadFile(file, "avatars");
        log.info("{} uploaded successfully for user {}", uploadedFile.getUrl(), existingUser.getEmail());

        User updatedUser = existingUser.toBuilder().profileImage(uploadedFile.getUrl()).build();
        userRepo.save(updatedUser);

        return uploadedFile;

    }

    /**
     * Verify user email using token
     * 
     * @param token
     */
    public void verifyEmail(String token) {
        User user = userRepo.findByVerificationToken(token)
                .orElseThrow(() -> new AuthorizationException("Invalid or already used verification token",
                        ErrorCode.INVALID_TOKEN));

        // Check if token is expired

        if (user.getTokenExpiration() != null && user.getTokenExpiration().isBefore(Instant.now())) {
            throw new AuthorizationException("Verification token has expired please request a new token ",
                    ErrorCode.TOKEN_EXPIRED);

        }

        // Check if user already verified
        if (user.isVerified()) {
            throw new AuthorizationException("Email is already verified kindly login ",
                    ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        // verify the user
        user = user.toBuilder().emailVerified(true).verificationToken(null).tokenExpiration(null).build();

        userRepo.save(user);
    }

    /**
     * Resend email verification token
     */
    public void resendUserVerificationEmail(String email) {
        User user = this.findByEmail(email);

        // Check if user already verified
        if (user.isVerified()) {
            throw new AuthorizationException("Email is already verified kindly login ",
                    ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        user = this.refreshVerificationTokenIfExpired(user);

        emailService.sendUserVerificationEmail(user.getEmail(), user.getFirstName(),
                user.getVerificationToken(), user.getTokenExpiration());

    }

    /**
     * Update user password. Ensure new password is different from old password
     * 
     * @param requestDto
     */
    public void updateUserPassword(ResetPasswordRequestDto requestDto) {
        try {
            User user = userRepo.findByPwdResetToken(requestDto.getToken()).orElseThrow(() -> {
                log.error("Invalid  or expired password reset token for {}", requestDto.getEmail());
                throw new AuthorizationException("Invalid or expired password reset token",
                        ErrorCode.INVALID_TOKEN);
            });

            if (user.getTokenExpiration() == null ||
                    user.getTokenExpiration().isBefore(Instant.now())) {
                log.error("expired password reset token for {}", requestDto.getEmail());

                throw new AuthorizationException(
                        "Password reset token has expired",
                        ErrorCode.TOKEN_EXPIRED);
            }

            // Check if new password is same as old password
            if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())) {
                log.error("{} used old password as new password", requestDto.getEmail());
                throw new AuthorizationException("New password must be different from old password",
                        ErrorCode.INVALID_INPUT);
            }

            // Encode and save new passsword
            String encodedNewPassword = passwordEncoder.encode(requestDto.getNewPassword());

            User updatedUser = user.toBuilder()
                    .password(encodedNewPassword)
                    .pwdResetToken(null)
                    .tokenExpiration(null)
                    .build();

            log.info("Password updated successfully for {}", requestDto.getEmail());

            userRepo.save(updatedUser);

        } catch (AuthorizationException e) {
            throw e;
        }

    }

    /**
     * Checks if email verification token has expired. Generates a new token if
     * expired
     */
    public User refreshVerificationTokenIfExpired(User user) {
        Instant now = Instant.now();

        boolean needsNewToken = user.getVerificationToken() == null ||
                user.getTokenExpiration() == null ||
                user.getTokenExpiration().isBefore(now);

        if (!needsNewToken) {
            return user; // existing token still valid
        }

        String newToken = tokenGenerator.generateToken();
        Instant newExpiry = now.plus(24, ChronoUnit.HOURS);

        User updatedUser = user.toBuilder()
                .verificationToken(newToken)
                .tokenExpiration(newExpiry)
                .build();

        return userRepo.save(updatedUser);
    }

}
