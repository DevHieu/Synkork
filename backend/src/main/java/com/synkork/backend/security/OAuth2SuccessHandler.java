package com.synkork.backend.security;

import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserService;
import com.synkork.backend.modules.user.enums.ProviderEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${frontend.client.url}")
    private String frontendUrl;

    @Autowired
    UserService userService;

    @Autowired
    JwtService  jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User =  (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        UserEntity existedUser = userService.findByEmail(email);

        if (existedUser != null) {

            existedUser.setProvider(ProviderEnum.GOOGLE);
            userService.updateUser(existedUser);
        } else {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setProvider(ProviderEnum.GOOGLE);
            existedUser = userService.create(newUser);
        }

        String refreshToken = jwtService.generateToken(existedUser.getId().toString(), email, "REFRESH");
        jwtService.saveRefreshToken(refreshToken, response);

        String accessToken = jwtService.generateToken(existedUser.getId().toString(), email, "ACCESS");
        response.sendRedirect(frontendUrl + "/oauth2/redirect?token=" + accessToken);
    }
}
