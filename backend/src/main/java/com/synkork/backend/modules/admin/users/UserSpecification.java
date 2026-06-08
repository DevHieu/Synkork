package com.synkork.backend.modules.admin.users;

import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.enums.RoleEnum;
import com.synkork.backend.modules.user.enums.UserStatusEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSpecification {

    public static Specification<UserEntity> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("username")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("displayName")), pattern)
            );
        };
    }

    public static Specification<UserEntity> hasRole(RoleEnum role) {
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
    }

    public static Specification<UserEntity> hasStatus(UserStatusEnum status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}