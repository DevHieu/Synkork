package com.synkork.backend.filter;

import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.space.SpaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.UUID;

@Component
public class SpaceAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private SpaceService spaceService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(
                        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE
                );

        String spaceIdStr =
                pathVariables != null
                        ? pathVariables.get("spaceId")
                        : null;

        if (spaceIdStr == null) {
            return true;
        }

        UUID spaceId = UUID.fromString(spaceIdStr);
        UUID currentUserId = AuthUtils.getCurrentUserId();

        boolean hasAccess =
                spaceService.checkUserAccess(spaceId, currentUserId);

        if (!hasAccess) {
            throw new AccessDeniedException(
                    "You don't have permission to access this space"
            );
        }

        return true;
    }
}