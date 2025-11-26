package com.tns.newscrawler.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    // Spring sẽ tự động tìm và inject CustomUserDetailsService vào đây
    private final UserDetailsService userDetailsService;

    // ==============================================================
    // 1. AUTHENTICATION PROVIDER (Cấu hình Logic Xác thực)
    // ==============================================================
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Bước A: Chỉ định cách tìm User trong DB
        authProvider.setUserDetailsService(userDetailsService);

        // Bước B: Chỉ định cách mã hóa mật khẩu (Fix lỗi "id null")
        // Bắt buộc dùng passwordEncoder() định nghĩa bên dưới
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // ==============================================================
    // 2. PASSWORD ENCODER (Mã hóa mật khẩu)
    // ==============================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Sử dụng thuật toán BCrypt chuẩn (Không cần prefix {bcrypt})
        return new BCryptPasswordEncoder();
    }

    // ==============================================================
    // 3. AUTHENTICATION MANAGER (Quản lý đăng nhập)
    // ==============================================================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ==============================================================
    // 4. MVC HANDLER (Fix lỗi Spring Boot 3)
    // ==============================================================
//    @Bean(name = "mvcHandlerMappingIntrospector")
//    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
//        return new HandlerMappingIntrospector();
//    }
}