package com.synkork.backend.common.utils;

import com.synkork.backend.security.UserPrinciple;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class AuthUtils {
    public static UUID getCurrentUserId() {
        UserPrinciple user = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    public static String getCurrentUsername() {
        UserPrinciple user = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUsername();
    }
}
