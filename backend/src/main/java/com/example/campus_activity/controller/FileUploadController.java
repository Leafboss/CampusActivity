package com.example.campus_activity.controller;

import com.example.campus_activity.pojo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传接口：接收前端传来的图片，存到本地 uploads/ 目录。
 * 数据库只存相对路径（如 /uploads/xxx.jpg），不存文件本体。
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    // 上传目录：读 application.yml 的 upload.path，默认是项目根目录下的 uploads/
    @Value("${upload.path:uploads/}")
    private String uploadPath;

    /**
     * 上传图片：POST /api/upload，表单字段名 file
     * 返回可访问的 URL 路径，前端拿到后存进活动的 image 字段
     */
    @PostMapping
    public Result upload(@RequestParam("file") MultipartFile file) {
        // 收到请求先打日志，方便排查「前端到底发没发出来」
        log.info("收到上传请求：原始文件名={}, 大小={}KB", file.getOriginalFilename(), file.getSize() / 1024);

        // 空文件直接拒绝
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        // 只允许常见图片格式，防止上传可执行文件
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!".jpg.jpeg.png.gif.webp".contains(suffix)) {
            return Result.error("只支持 jpg/jpeg/png/gif/webp 图片");
        }

        // 用 UUID 重命名，避免不同用户传同名文件互相覆盖
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 确保目录存在（第一次上传时自动创建 uploads/）
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 落盘
        try {
            file.transferTo(new File(dir, fileName));
            log.info("上传成功：保存到 {}", new File(dir, fileName).getAbsolutePath());
        } catch (IOException e) {
            log.error("保存文件失败", e);
            return Result.error("保存文件失败：" + e.getMessage());
        }

        // 返回前端可访问的路径（WebConfig 会把 /uploads/** 映射到这个目录）
        return Result.success("/uploads/" + fileName);
    }
}
