package com.example.campus_activity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web 配置：把本地 uploads/ 目录暴露成可访问的静态资源路径。
 * 否则前端访问 /uploads/xxx.jpg 会 404（文件在磁盘上，Spring 不知道要把它当静态资源）。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // file: 前缀告诉 Spring 这是磁盘路径；末尾拼绝对路径，避免相对路径歧义
        String absolutePath = new File(uploadPath).getAbsolutePath() + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
