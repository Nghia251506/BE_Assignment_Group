package com.tns.newscrawler.security;

import com.tns.newscrawler.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Chuỗi base64 (ít nhất 256-bit). Đây chỉ là ví dụ, anh tự generate chuỗi mạnh hơn nhé.
    private static final String SECRET_KEY = "bXktc3VwZXItc2VjcmV0LXN1cGVyLXNlY3JldC1rZXktMTIzNDU2Nzg5MA==";

    private final SecretKey key =
            Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

    // ---- Tạo token ----
    public String generateToken(User user) { // ← ĐỔI THAM SỐ THÀNH User ĐỂ LẤY ROLE!!!
        Instant now = Instant.now();
        Instant expiry = now.plus(1, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(user.getUsername())                    // username
                .claim("userId", user.getId())                  // NHỒI ID
                .claim("roleName", user.getRole().getName())    // NHỒI ROLE NAME
                .claim("roleCode", user.getRole().getCode())    // NHỒI ROLE CODE
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
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

    // ---- Validate token ----
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token); // ← Vẫn dùng parseSignedClaims bình thường
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
