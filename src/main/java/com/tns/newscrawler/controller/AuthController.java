package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.Auth.LoginRequest;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.service.User.UserServiceImpl;
import com.tns.newscrawler.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
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
            // Kiểm tra thông tin đăng nhập
            if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.status(400).body("Tên đăng nhập và mật khẩu không được để trống");
            }

            // Thực hiện xác thực người dùng
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // Đặt Authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lấy thông tin username từ authentication
            String username = authentication.getName();

            // Lấy thông tin User từ database
            com.tns.newscrawler.entity.User userEntity = userService.findByUsername(username);

            // Kiểm tra nếu không tìm thấy user trong DB
            if (userEntity == null) {
                return ResponseEntity.status(404).body("User không tồn tại");
            }

            // Tạo JWT token cho user đã đăng nhập
            String jwtToken = jwtTokenProvider.generateToken(userEntity);

            // Set cookie với JWT token
            Cookie cookie = new Cookie("access_token", jwtToken);
//            cookie.setHttpOnly(true); // Cấm javascript truy cập cookie
            cookie.setSecure(true); // Chỉ gửi cookie qua HTTPS
            cookie.setPath("/"); // Cookie có hiệu lực trên toàn bộ ứng dụng
            cookie.setMaxAge(7 * 24 * 60 * 60); // Cookie tồn tại trong 7 ngày
            response.addCookie(cookie);

            // Lấy thông tin UserDto từ service
            UserDto userDto = userService.getByUsername(username);

            // Trả về UserDto cùng với mã trạng thái 200 OK
            return ResponseEntity.ok(userDto);

        } catch (BadCredentialsException e) {
            // Xử lý khi thông tin đăng nhập sai
            return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu");
        } catch (Exception e) {
            // Xử lý lỗi chung
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

        Cookie cookie = new Cookie("access_token", null);
//        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Đăng xuất thành công");
    }
}