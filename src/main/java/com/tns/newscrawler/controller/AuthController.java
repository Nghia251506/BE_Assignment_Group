package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.User.UserDto;
import com.tns.newscrawler.dto.Auth.LoginRequest;
import com.tns.newscrawler.service.User.UserServiceImpl;
import com.tns.newscrawler.security.JwtTokenProvider;
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
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String username = loginRequest.getUsername();
            String jwtToken = jwtTokenProvider.generateToken(username);

            // Set cookie đúng chuẩn production (SameSite=None + Secure)
            setAuthCookie(response, jwtToken);

            UserDto userDto = userService.getByUsername(username);
            return ResponseEntity.ok(userDto);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        UserDto current = userService.getCurrentUser();
        return current != null ? ResponseEntity.ok(current) : ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        clearAuthCookie(response);
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    // SET COOKIE HOÀN HẢO – KHÔNG BAO GIỜ LỖI NỮA
    private void setAuthCookie(HttpServletResponse response, String token) {
        boolean isProd = isProduction();

        String cookieValue = String.join("; ",
                "access_token=" + token,
                "Path=/",
                "HttpOnly",
                "Max-Age=" + (7 * 24 * 60 * 60), // 7 ngày
                "SameSite=" + (isProd ? "None" : "Lax"),
                isProd ? "Secure" : ""
        );

        response.addHeader("Set-Cookie", cookieValue);
    }

    // XÓA COOKIE HOÀN HẢO
    private void clearAuthCookie(HttpServletResponse response) {
        boolean isProd = isProduction();

        String cookieValue = String.join("; ",
                "access_token=",
                "Path=/",
                "HttpOnly",
                "Max-Age=0",
                "Expires=Thu, 01 Jan 1970 00:00:00 GMT",
                "SameSite=" + (isProd ? "None" : "Lax"),
                isProd ? "Secure" : ""
        );

        response.addHeader("Set-Cookie", cookieValue);
    }

    // Detect môi trường production
    private boolean isProduction() {
        return environment.acceptsProfiles(Profiles.of("prod"));
    }
}
