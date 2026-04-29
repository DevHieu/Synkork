package com.synkork.backend.common.utils;

import com.synkork.backend.common.dtos.FileUploaded;
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

    public FileUploaded uploadFile(MultipartFile file, String folderName) {
        try {
            Map options = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "raw"
            );
            Map uploaded = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploaded.get("public_id");
            // raw file không dùng cloudinary.url() được, lấy thẳng secure_url
            String url = (String) uploaded.get("secure_url");

            return new FileUploaded(url, publicId, "raw", file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Upload file failed", e);
        }
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
