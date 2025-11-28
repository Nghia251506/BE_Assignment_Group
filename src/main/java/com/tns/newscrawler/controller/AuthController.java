package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.Auth.LoginRequest;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.service.User.UserServiceImpl;
import com.tns.newscrawler.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // Validate input
            if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.status(400).body("Tên đăng nhập và mật khẩu không được để trống");
            }

            // Xác thực bằng Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String username = authentication.getName();
            User userEntity = userService.findByUsername(username);

            if (userEntity == null) {
                return ResponseEntity.status(404).body("User không tồn tại");
            }

            // TẠO JWT NHƯ CŨ
            String jwtToken = jwtTokenProvider.generateToken(userEntity);

            // Set cookie với JWT token
            Cookie cookie = new Cookie("access_token", jwtToken);
            cookie.setHttpOnly(true); // Cấm javascript truy cập cookie
            cookie.setSecure(true); // Chỉ gửi cookie qua HTTPS
            cookie.setPath("/"); // Cookie có hiệu lực trên toàn bộ ứng dụng
            cookie.setMaxAge(7 * 24 * 60 * 60); // Cookie tồn tại trong 7 ngày
            cookie.setDomain("admin.muong14.xyz");        // CHỈ gửi cookie cho đúng admin subdomain
            cookie.setAttribute("SameSite", "None");
            response.addCookie(cookie);

            // Trả về thông tin user
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
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // XÓA SESSION → JWT TRONG SESSION BIẾN MẤT
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Đăng xuất thành công");
    }
}