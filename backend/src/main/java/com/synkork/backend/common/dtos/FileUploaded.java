package com.synkork.backend.common.dtos;

public record FileUploaded(
        String url,
        String publicId,
        String resourceType,  // "image" hoặc "raw"
        String originalName
) {}