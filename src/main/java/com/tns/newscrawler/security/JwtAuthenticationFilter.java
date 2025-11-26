package com.tns.newscrawler.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // BẮT BUỘC PHẢI CÓ DÒNG NÀY
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        System.out.println("========== JWT FILTER START ==========");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());

        try {
            String jwt = getJwtFromRequest(request);
            System.out.println("JWT extracted: " + (jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) + "..." : "NULL"));

            if (StringUtils.hasText(jwt)) {
                boolean isValid = jwtTokenProvider.validateToken(jwt);
                System.out.println("Token valid: " + isValid);

                if (isValid) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(jwt);

                    if (authentication != null) {
                        System.out.println("Authentication created:");
                        System.out.println("  - Principal: " + authentication.getPrincipal());
                        System.out.println("  - Name: " + authentication.getName());
                        System.out.println("  - Authorities: " + authentication.getAuthorities());
                        System.out.println("  - Is Authenticated: " + authentication.isAuthenticated());

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        System.out.println("Authentication set in SecurityContext");
                    } else {
                        System.out.println("Authentication is NULL!");
                    }
                }
            } else {
                System.out.println("No JWT token found in request");
            }
        } catch (Exception ex) {
            System.err.println("ERROR in JWT Filter: " + ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("========== JWT FILTER END ==========");
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Ưu tiên lấy từ Header: Authorization = Bearer <token>
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7); // bỏ "Bearer "
            System.out.println("Found JWT in Authorization header: " +
                    (token.length() > 20 ? token.substring(0, 20) + "..." : token));
            return token;
        }

        // 2. Nếu không có trong header → thử lấy từ cookie (thường dùng khi SPA + HttpOnly cookie)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName()) || "jwt".equals(cookie.getName()) || "token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    System.out.println("Found JWT in cookie '" + cookie.getName() + "': " +
                            (token.length() > 20 ? token.substring(0, 20) + "..." : token));
                    return token;
                }
            }
        }

        // 3. Một số dự án cũ gửi token trong query parameter hoặc header X-...
        String tokenFromParam = request.getParameter("access_token");
        if (StringUtils.hasText(tokenFromParam)) {
            System.out.println("Found JWT in query param: " + tokenFromParam.substring(0, Math.min(20, tokenFromParam.length())) + "...");
            return tokenFromParam;
        }

        // 4. Header tùy chỉnh (ít dùng)
        String customHeader = request.getHeader("X-Auth-Token");
        if (StringUtils.hasText(customHeader)) {
            System.out.println("Found JWT in X-Auth-Token header");
            return customHeader;
        }

        System.out.println("No JWT found in any location");
        return null;
    }
}