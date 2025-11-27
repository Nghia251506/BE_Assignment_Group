package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.Auth.LoginRequest;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.service.User.UserServiceImpl;
import com.tns.newscrawler.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserServiceImpl userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final Environment environment;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.status(400).body("Tên đăng nhập và mật khẩu không được để trống");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String username = authentication.getName();
            User userEntity = userService.findByUsername(username);

            if (userEntity == null) {
                return ResponseEntity.status(404).body("User không tồn tại");
            }

            String jwtToken = jwtTokenProvider.generateToken(userEntity);

            // FIX HOÀN HẢO: DÙNG ResponseCookie + TỰ ĐỘNG LOCAL vs PRODUCTION
            boolean isProduction = environment.acceptsProfiles(Profiles.of("prod"))
                    || !environment.acceptsProfiles(Profiles.of("local", "dev"));

            ResponseCookie cookie = ResponseCookie.from("access_token", jwtToken)
                    .httpOnly(true)                          // BẬT LẠI – AN TOÀN TUYỆT ĐỐI
                    .secure(isProduction)                    // local = false → không bị block
                    .sameSite(isProduction ? "None" : "Lax") // production = None, local = Lax
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .domain(isProduction ? ".muong14.xyz" : null) // chia sẻ subdomain ở production
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());

            UserDto userDto = userService.getByUsername(username);
            return ResponseEntity.ok(userDto);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserDto userDto = userService.getByUsername(username);
            return ResponseEntity.ok(userDto);
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        boolean isProduction = environment.acceptsProfiles(Profiles.of("prod"))
                || !environment.acceptsProfiles(Profiles.of("local", "dev"));

        ResponseCookie cookie = ResponseCookie.from("access_token", null)
                .httpOnly(true)
                .secure(isProduction)
                .sameSite(isProduction ? "None" : "Lax")
                .path("/")
                .maxAge(0)
                .domain(isProduction ? ".muong14.xyz" : null)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok("Đăng xuất thành công");
    }
}
