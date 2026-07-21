package com.synkork.backend.common.utils;

import com.synkork.backend.modules.roomMember.RoomMemberRepository;
import com.synkork.backend.modules.roomMember.enums.RoomMemberRoleEnum;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PlanLimitUtils {

    private static RoomMemberRepository roomMemberRepository;

    public PlanLimitUtils(RoomMemberRepository roomMemberRepository) {
        PlanLimitUtils.roomMemberRepository = roomMemberRepository;
    }

    public static int maxRooms(PlanEnum plan) {
        return switch (plan) {
            case FREE     -> 5;
            case TEAM     -> 10;
            case BUSINESS -> 30;
        };
    }

    public static int maxChatSpaces(PlanEnum plan) {
        return switch (plan) {
            case FREE     -> 3;
            case TEAM     -> 10;
            case BUSINESS -> 20;
        };
    }

    public static int maxVoiceSpaces(PlanEnum plan) {
        return switch (plan) {
            case FREE     -> 2;
            case TEAM     -> 5;
            case BUSINESS -> 10;
        };
    }

    public static int maxCollaborationSpaces(PlanEnum plan) {
        return switch (plan) {
            case FREE     -> 1;
            case TEAM     -> 3;
            case BUSINESS -> 10;
        };
    }

    public static long maxFileSizeBytes(PlanEnum plan) {
        return switch (plan) {
            case FREE     -> 1L * 1024 * 1024;
            case TEAM     -> 10L * 1024 * 1024;
            case BUSINESS -> 50L * 1024 * 1024;
        };
    }

    public static boolean checkMaxRooms(PlanEnum userPlan, UUID userId) {
        int maxRooms = maxRooms(userPlan);
        long currentRooms = roomMemberRepository.countGroupRoomsByUserIdAndRole(
                userId, RoomMemberRoleEnum.OWNER);

        if (currentRooms >= maxRooms) {
            throw new RuntimeException(
                    "Gói " + userPlan + " chỉ được tạo tối đa " + maxRooms + " room. Vui lòng nâng cấp gói.");
        }

        return true;
    }
}