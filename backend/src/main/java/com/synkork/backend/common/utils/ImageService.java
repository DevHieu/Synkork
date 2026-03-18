package com.synkork.backend.common.utils;

import com.synkork.backend.common.dtos.ImageCreated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ImageService {

    @Autowired
    private Cloudinary cloudinary;

    public ImageCreated uploadImage(MultipartFile file, String folderName) {
        try{
            HashMap<Object, Object> options = new HashMap<>();
            options.put("folder", folderName);
            Map uploadedFile = this.cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadedFile.get("public_id");
            String url = cloudinary.url().secure(true).generate(publicId);


            return new ImageCreated(url, publicId);

        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteImage(String publicId) {
        try {
            System.out.println("=== DELETING: '" + publicId + "' ===");
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
            System.out.println("Delete result: " + result); // thêm dòng này
            return "ok".equals(result.get("result"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
