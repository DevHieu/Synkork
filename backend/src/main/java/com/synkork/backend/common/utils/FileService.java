package com.synkork.backend.common.utils;

import com.synkork.backend.common.dtos.FileUploaded;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class FileService {

    @Autowired
    private Cloudinary cloudinary;

    public FileUploaded uploadImage(MultipartFile file, String folderName) {
        try {
            Map options = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "image"
            );
            Map uploaded = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploaded.get("public_id");
            String url = cloudinary.url().secure(true).generate(publicId);

            return new FileUploaded(url, publicId, "image", file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Upload image failed", e);
        }
    }

    public FileUploaded uploadFile(MultipartFile file, String folderName, PlanEnum plan, boolean needSizeCheck) {
        try {
            if (needSizeCheck) {
                long maxSize = PlanLimitUtils.maxFileSizeBytes(plan);
                if (file.getSize() > maxSize) {
                    long maxMB = maxSize / (1024 * 1024);
                    throw new RuntimeException(
                            "File vượt quá giới hạn " + maxMB + "MB của gói " + plan + ". Vui lòng nâng cấp gói."
                    );
                }
            }

            Map options = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "raw"
            );
            Map uploaded = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploaded.get("public_id");
            String url = (String) uploaded.get("secure_url");

            return new FileUploaded(url, publicId, "raw", file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Upload file failed", e);
        }
    }

    public FileUploaded uploadFile(MultipartFile file, String folderName, boolean needSizeCheck) {
        return uploadFile(file, folderName, PlanEnum.FREE, needSizeCheck);
    }

    public FileUploaded uploadFile(MultipartFile file, String folderName) {
        return uploadFile(file, folderName, PlanEnum.FREE, true);
    }

    public boolean deleteFile(String publicId, String resourceType) {
        try {
            Map result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("invalidate", true, "resource_type", resourceType)
            );
            return "ok".equals(result.get("result"));
        } catch (IOException e) {
            throw new RuntimeException("Delete file failed", e);
        }
    }
}