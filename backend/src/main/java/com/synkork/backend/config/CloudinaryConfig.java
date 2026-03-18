package com.synkork.backend.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.name}")
    private String cloudinaryName;

    @Value("${cloudinary.key}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.secret}")
    private String cloudinaryApiSecret;
    @Bean
    public Cloudinary getCloudinary(){
        Map config = new HashMap();
        config.put("cloud_name", cloudinaryName);
        config.put("api_key", cloudinaryApiKey);
        config.put("api_secret", cloudinaryApiSecret);
        config.put("secure", true);
        return new Cloudinary(config);
    }
}
