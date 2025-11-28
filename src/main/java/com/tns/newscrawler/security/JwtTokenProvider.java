package com.tns.newscrawler.security;

import com.tns.newscrawler.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final String SECRET_KEY = "bXktc3VwZXItc2VjcmV0LXN1cGVyLXNlY3JldC1rZXktMTIzNDU2Nzg5MA==";

    private final SecretKey key =
            Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

    // ---- Tạo token ----
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(1, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("roleCode", "ROLE_" + user.getRole().getCode())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    // ---- Lấy Authentication từ token ----
    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String roleCode = claims.get("roleCode", String.class);

            if (username == null || roleCode == null || roleCode.isEmpty()) {
                System.err.println("JWT missing username or roleCode");
                return null;
            }

            // ĐẢM BẢO ROLE CÓ "ROLE_" ĐẦU
            String authorityName = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
            GrantedAuthority authority = new SimpleGrantedAuthority(authorityName);
            List<GrantedAuthority> authorities = Collections.singletonList(authority);

            // BỎ HOÀN TOÀN WebAuthenticationDetailsSource() → KHÔNG CẦN REQUEST Ở ĐÂY!
            return new UsernamePasswordAuthenticationToken(username, null, authorities);

        } catch (Exception e) {
            System.err.println("ERROR parsing JWT for authentication: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ---- Lấy username từ token ----
    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Lấy roleCode từ token ----
    public String getRoleCodeFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("roleCode", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Lấy userId từ token (optional) ----
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ---- Validate token ----
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
