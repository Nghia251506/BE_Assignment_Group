package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.Auth.LoginRequest;
import com.tns.newscrawler.entity.User;
import com.tns.newscrawler.mapper.User.UserMapper;
import com.tns.newscrawler.service.User.UserServiceImpl;
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
    private final UserServiceImpl userService; // hoặc UserService nếu anh thêm method vào interface

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequest request) {
        try {
            Authentication authToken =
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword());

            Authentication auth = authenticationManager.authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = userService.getDomainUserByUsername(request.getUsername());
            return ResponseEntity.ok(UserMapper.toDto(user));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).build();
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
