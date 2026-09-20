package com.sky.takeout.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConditionalOnBean(UploadProperties.class)
public class UploadResourceConfig implements WebMvcConfigurer {

    private final UploadProperties uploadProperties;

    public UploadResourceConfig(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDirectory = Paths.get(uploadProperties.getPath())
                .toAbsolutePath()
                .normalize();

        String location = uploadDirectory.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler(uploadProperties.getUrlPrefix() + "/**")
                .addResourceLocations(location);
    }
}
