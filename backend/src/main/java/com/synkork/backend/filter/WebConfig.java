package com.synkork.backend.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SpaceAccessInterceptor spaceAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(spaceAccessInterceptor)
                .addPathPatterns(
                        "/spaces/{spaceId}/**",
                        "/rooms/{roomId}/spaces/{spaceId}/**",
                        "/calendar-events/{spaceId}/**"
                );
    }
}
