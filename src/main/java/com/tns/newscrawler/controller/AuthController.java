package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.Auth.LoginRequest;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.mapper.User.UserMapper;
import com.tns.newscrawler.security.JwtTokenProvider;
import com.tns.newscrawler.service.User.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private final UserServiceImpl userService; // hoặc UserService nếu anh thêm method vào interface
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            // Thực hiện xác thực người dùng
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // Lưu thông tin người dùng vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lấy thông tin người dùng từ database và tạo JWT token
            String jwtToken = jwtTokenProvider.generateToken(loginRequest.getUsername());

            // Tạo cookie với JWT
            Cookie cookie = new Cookie("token", jwtToken); // Tạo cookie với tên "token" và giá trị là JWT
            cookie.setHttpOnly(true); // Chỉ có thể truy cập từ phía server, không cho JS truy cập
            cookie.setSecure(true); // Chỉ gửi cookie qua HTTPS
            cookie.setPath("/"); // Cookie này sẽ áp dụng cho tất cả các request trên domain này
            cookie.setMaxAge(3600); // Thời gian sống của cookie (1 giờ)

            // Thêm cookie vào response
            response.addCookie(cookie);

            // Trả về JWT trong response header (có thể không cần thiết nữa khi gửi cookie)
            return ResponseEntity.ok("Login successful");

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        UserDto current = userService.getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(current);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        try {
            // 1. Xoá context đăng nhập trên server
            SecurityContextHolder.clearContext();

            // 2. Gửi cookie "token" rỗng về để xoá trên browser
            Cookie cookie = new Cookie("token", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);      // nếu lúc login anh cũng set secure
            cookie.setPath("/");         // phải trùng path với cookie lúc tạo
            cookie.setMaxAge(0);         // 0 = yêu cầu xoá cookie
            response.addCookie(cookie);

            // 3. Trả message cho FE
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            // Nếu muốn log lỗi:
            // log.error("Error while logging out", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed");
        }
    }
}
