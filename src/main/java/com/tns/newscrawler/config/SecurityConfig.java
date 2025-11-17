package com.tns.newscrawler.config;

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

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

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

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .userDetailsService(customUserDetailsService)
//                .authorizeHttpRequests(auth -> auth
//                        // cho phép public mấy route client
//                        .requestMatchers(
//                                "/swagger-ui.html",
//                                "/swagger-ui/index.html/**",
//                                "/v3/api-docs/**",      // Đảm bảo API docs không yêu cầu login
//                                "/swagger-resources/**", // Tài nguyên Swagger UI
//                                "/webjars/**"           // Tài nguyên Swagger UI
//                        ).permitAll()
//                        .requestMatchers(
//                                "/",
//                                "/article/**",
//                                "/category/**",
//                                "/api/public/**",
//                                "/api/admin/posts",
//                                "/api/admin/categories/tenant/1"
//                        ).permitAll()
//
//                        // cho phép login/logout
//                        .requestMatchers("/api/auth/**").permitAll()
//
//                        // admin
//                        .requestMatchers("/api/admin/**").permitAll()
//
//                        // còn lại phải login
//                        .anyRequest().authenticated()
//                )
//                // Dùng formLogin hoặc httpBasic, tuỳ anh
//                .httpBasic(basic -> {})  // → test Postman cho dễ
//                .formLogin(form -> form
//                        .loginPage("/login")       // nếu anh có trang login custom
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .permitAll()
//                );
//
//        return http.build();
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // Tắt CSRF protection
                .authorizeRequests()
                .requestMatchers("/**").permitAll()  // Thay thế antMatchers bằng requestMatchers
                .anyRequest().permitAll()     // Bỏ qua tất cả các yêu cầu bảo mật
                .and()
                .formLogin().disable() // Nếu đang sử dụng formLogin thì tắt nó
                .httpBasic().disable(); // Tắt HTTP Basic authentication

        return http.build();
    }
}
