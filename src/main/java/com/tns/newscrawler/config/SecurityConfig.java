package com.tns.newscrawler.config;

import com.tns.newscrawler.security.JwtAuthenticationFilter;
import com.tns.newscrawler.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService customUserDetailsService;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cho phép inject AuthenticationManager nếu cần dùng trong AuthController
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF protection
                .userDetailsService(customUserDetailsService) // Cấu hình dịch vụ người dùng tùy chỉnh
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**"
                        ).permitAll() // Cho phép Swagger không cần xác thực
                        .requestMatchers(
                                "/", "/article/**", "/category/**", "/api/public/**"
                        ).permitAll() // Các route client không yêu cầu login
                        .requestMatchers("/api/auth/**").permitAll() // Cho phép các API auth không cần login
                        .requestMatchers("/api/admin/**").permitAll() // Admin routes yêu cầu quyền ADMIN
                        .anyRequest().authenticated() // Các request còn lại yêu cầu login
                )

                .httpBasic(basic -> {}); // Sử dụng HTTP Basic Authentication (thường dùng trong Postman)

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
