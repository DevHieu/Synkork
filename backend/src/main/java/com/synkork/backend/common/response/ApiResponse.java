package com.synkork.backend.common.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Object meta
) {

    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {
        return new ApiResponse<>(
                true,
                message,
                data,
                null
        );
    }

    public static <T> ApiResponse<T> success(
            String message,
            T data,
            Object meta
    ) {
        return new ApiResponse<>(
                true,
                message,
                data,
                meta
        );
    }

    public static <T> ApiResponse<T> error(
            String message,
            T data
    ) {
        return new ApiResponse<>(
                false,
                message,
                data,
                null
        );
    }
}