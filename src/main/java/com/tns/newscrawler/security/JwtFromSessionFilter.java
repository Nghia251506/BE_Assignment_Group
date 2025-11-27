package com.tns.newscrawler.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFromSessionFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String jwt = (String) session.getAttribute("jwt_token");
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                Authentication auth = jwtTokenProvider.getAuthentication(jwt);
                if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("SUCCESS: Authenticated user: " + auth.getName()
                            + " | Roles: " + auth.getAuthorities());
                } else {
                    System.out.println("FAILED: getAuthentication() returned null or empty authorities");
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}