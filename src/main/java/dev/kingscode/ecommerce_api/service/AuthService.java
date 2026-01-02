package dev.kingscode.ecommerce_api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kingscode.ecommerce_api.dto.user.request.LoginRequestDto;
import dev.kingscode.ecommerce_api.dto.user.response.LoginResponseDto;
import dev.kingscode.ecommerce_api.exception.AuthenticationException;
import dev.kingscode.ecommerce_api.model.User;
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

    /**
     * Authenticates user and returns generated JWT and RefreshToken
     * 
     * @return
     */
    public LoginResponseDto authenticate(LoginRequestDto loginRequest) {
        try {
            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            // load user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate JWT access and refresh token
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            User user = userService.findByEmail(loginRequest.getEmail());

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
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            throw new AuthenticationException("Authentication failed");
        }
    }

}
