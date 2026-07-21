package com.synkork.backend.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.crypto.SecretKey;

import com.synkork.backend.modules.auth.dto.JwtResponse;
import com.synkork.backend.modules.user.enums.RoleEnum;
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

    public String generateToken(String userId, String username, RoleEnum role, String type) {
        String roleString = role.toString();

        long duration = type.equals("ACCESS")
                ? TimeUnit.MINUTES.toMillis(60) // Access key hết hạn sau 60p
                : TimeUnit.DAYS.toMillis(7); // Refresh key thì 7 ngày

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + duration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", type);
        claims.put("userId", userId);
        claims.put("role", roleString);
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

    public String generateJwtToken(String userId, String username, RoleEnum role, HttpServletResponse response) {
        String accessToken = this.generateToken(userId, username, role, "ACCESS");
        String refreshToken = this.generateToken(userId, username, role, "REFRESH");

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

    public String generateShortLivedState(String userId, String redirectPath) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + TimeUnit.MINUTES.toMillis(10));

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "OAUTH_STATE");
        claims.put("userId", userId);
        if (redirectPath != null) {
            claims.put("redirectPath", redirectPath);
        }

        return Jwts.builder()
                .claims().add(claims).and()
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getKey())
                .compact();
    }

    public Claims validateAndExtractState(String state) {
        Claims claims = extractAllClaims(state);
        String type = claims.get("type", String.class);
        if (!"OAUTH_STATE".equals(type)) {
            throw new IllegalArgumentException("Invalid state token type");
        }
        return claims;
    }
}