package com.synkork.backend.filter;

import com.synkork.backend.security.JwtService;
import com.synkork.backend.security.MyUserDetailService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
// Những lớp ở folder filter sẽ chứa các bộ lọc (filter) cho ứng dụng
// Ví dụ như class này sẽ là bộ lọc JWT (JSON Web Token) để xác thực các yêu cầu đến
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MyUserDetailService userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Bỏ qua các request OPTIONS cho CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtService.extractUserName(token);
                String tokenType = jwtService.extractClaim(token, claims -> claims.get("type", String.class));

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailService.loadUserByUsername(username);

                    // CHỈ xác thực nếu là loại ACCESS
                    if ("ACCESS".equals(tokenType) && jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        logger.error("Auth failed - tokenType: " + tokenType + ", valid: " + jwtService.validateToken(token, userDetails));
                    }
                }
            } catch (ExpiredJwtException e) {
                logger.error("JWT expired: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"TOKEN_EXPIRED\", \"message\": \"Access token has expired\"}");
                return; // Không gọi filterChain nữa
            } catch (Exception e) {
                logger.error("JWT validation failed: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"INVALID_TOKEN\", \"message\": \"Invalid token\"}");
                return;
            }
        }

        filterChain.doFilter(request, response); // Luôn luôn phải gọi dòng này
    }
}
