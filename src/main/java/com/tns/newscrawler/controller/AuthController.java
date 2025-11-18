package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.Auth.LoginRequest;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.mapper.User.UserMapper;
import com.tns.newscrawler.security.JwtTokenProvider;
import com.tns.newscrawler.service.User.UserServiceImpl;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Thực hiện xác thực người dùng
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // Lưu thông tin người dùng vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lấy thông tin người dùng từ database và tạo JWT token
            String jwtToken = jwtTokenProvider.generateToken(loginRequest.getUsername());

            // Trả về JWT token cho người dùng
            return ResponseEntity.ok(jwtToken);
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
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext(); // nếu dùng session, FE gọi thêm /logout của Spring cũng được
        return ResponseEntity.ok().build();
    }
}
