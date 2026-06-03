package com.synkork.backend.modules.user;

import com.synkork.backend.modules.user.enums.PlanEnum;

public class PlanLimitService {

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

    public static int maxNoteSpaces(PlanEnum plan) {
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
}