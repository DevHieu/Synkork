package com.synkork.backend.security;

import com.synkork.backend.modules.space.SpaceService;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
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
import java.util.Map;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${frontend.client.url}")
    private String frontendUrl;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    JwtService  jwtService;
    @Autowired
    private SpaceService spaceService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User =  (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        // Đoạn này dùng repo để check để còn cho cái trường hợp null. Chứ trong service đang trả về lỗi luôn thaành ra không tạo tk bằng google được
        UserEntity existedUser = userRepository.findByEmail(email).orElse(null);

        if (existedUser != null) {
            existedUser.setProvider(ProviderEnum.GOOGLE);
            // Sau OAuth login, bảo đảm Personal Calendar có ID để người dùng mở được lịch cá nhân.
            Map<String, UUID> personalId = spaceService.createPersonalSpaces(existedUser);
            existedUser.setPersonalNoteId(personalId.get("noteId"));
            // Lưu ID Calendar vừa được tìm hoặc tạo để sử dụng ở các lần đăng nhập sau.
            existedUser.setPersonalCalendarId(personalId.get("calendarId"));
            userService.updateUser(existedUser);
        } else {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setDisplayName(oAuth2User.getAttribute("name"));

            // Lấy email làm username
            String username = email.substring(0, email.indexOf("@"));
            newUser.setUsername(username);

            newUser.setProvider(ProviderEnum.GOOGLE);

            existedUser = userService.create(newUser);

            // Tạo hoặc khôi phục Personal Calendar sau lần đăng nhập OAuth đầu tiên.
            Map<String, UUID> personalId = spaceService.createPersonalSpaces(existedUser);
            existedUser.setPersonalNoteId(personalId.get("noteId"));
            // Lưu ID Calendar để frontend có thể điều hướng đến lịch cá nhân.
            existedUser.setPersonalCalendarId(personalId.get("calendarId"));

            userService.updateUser(existedUser);
        }

        String refreshToken = jwtService.generateToken(existedUser.getId().toString(), email, existedUser.getRole(), "REFRESH");
        jwtService.saveRefreshToken(refreshToken, response);

        String accessToken = jwtService.generateToken(existedUser.getId().toString(), email, existedUser.getRole(), "ACCESS");
        response.sendRedirect(frontendUrl + "/oauth2/redirect?token=" + accessToken);
    }
}
