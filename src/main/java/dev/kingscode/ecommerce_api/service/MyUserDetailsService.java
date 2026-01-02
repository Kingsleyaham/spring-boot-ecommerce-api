package dev.kingscode.ecommerce_api.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dev.kingscode.ecommerce_api.model.User;
import dev.kingscode.ecommerce_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepo.findByEmail(email).orElseThrow(() -> {
            log.warn("user not found with email: {}", email);
            return new UsernameNotFoundException("User not found with email " + email);
        });

        log.debug("User found: {} with role: {}", user.getEmail(), user.getRole());

        return user;

    }

}
