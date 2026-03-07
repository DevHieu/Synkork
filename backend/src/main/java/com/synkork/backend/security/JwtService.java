package com.synkork.backend.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.crypto.SecretKey;

import com.synkork.backend.modules.auth.dto.JwtResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretkey = "";

    public String generateToken(String username, String type) {
        long duration = type.equals("ACCESS")
                ? TimeUnit.MINUTES.toMillis(15) // Access key hết hạn sau 15p
                : TimeUnit.DAYS.toMillis(7); // Refresh key thì 7 ngày

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + duration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", type);
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretkey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token); // Lấy email từ token
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean validateRefreshToken(String token) {
        System.out.println("Verifying refresh token...");
        try {
            final Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            System.out.println(type);
            return "REFRESH".equals(type) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String generateJwtToken(String username, HttpServletResponse response) {
        String accessToken = this.generateToken(username, "ACCESS");
        String refreshToken = this.generateToken(username, "REFRESH");

        this.saveRefreshToken(refreshToken, response);

        return accessToken;
    }

    public void saveRefreshToken(String refreshToken, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}