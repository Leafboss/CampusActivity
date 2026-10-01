package com.example.campus_activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类：项目入口，运行 main 方法即可启动整个后端服务（端口 4987，见 application.yml）
 */
@SpringBootApplication
public class CampusActivityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusActivityApplication.class, args);
    }
}
