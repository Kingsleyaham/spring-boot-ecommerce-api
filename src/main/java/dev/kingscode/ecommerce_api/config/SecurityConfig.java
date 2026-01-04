package dev.kingscode.ecommerce_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import dev.kingscode.ecommerce_api.model.enums.UserRole;
import dev.kingscode.ecommerce_api.security.JwtAccessDeniedHandler;
import dev.kingscode.ecommerce_api.security.JwtAuthenticationEntryPoint;
import dev.kingscode.ecommerce_api.security.filter.JwtAuthFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
        private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

        private final JwtAuthFilter jwtFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http.csrf(customizer -> customizer.disable()).authorizeHttpRequests(
                                request -> request
                                                .requestMatchers("/api/v1/auth/*").permitAll()
                                                .requestMatchers(
                                                                "/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/docs/**",
                                                                "/",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .requestMatchers("/api/v1/admin/*")
                                                .hasAuthority(UserRole.ADMIN.toString())
                                                .anyRequest()
                                                .authenticated())
                                // .httpBasic(Customizer.withDefaults())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                                .accessDeniedHandler(jwtAccessDeniedHandler))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();

        }

        @Bean
        public OpenAPI customizeOpenAPI() {
                final String securitySchemeName = "bearerAuth";

                return new OpenAPI()
                                // .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                                .components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                                .name(securitySchemeName).type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer").bearerFormat("JWT")
                                                .description("JWT authentication")));
        }

}
