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
import java.util.List;
import java.util.UUID;

/**
 * 文件上传接口：接收前端传来的图片，存到本地 uploads/ 目录。
 * 数据库只存相对路径（如 /uploads/xxx.jpg），不存文件本体。
 *
 * 校验策略（本地存储的演示项目，刻意保持简单）：
 *   后缀白名单：只放行常见图片后缀，用 List 精确匹配，不在名单里直接拒绝；
 *   需要说明的是，这一层看的是「文件名后缀」，挡不住刻意改后缀的文件。
 *   本项目不做文件内容嗅探：生产环境应交给云对象存储（OSS/S3），
 *   内容检测与访问控制由云服务提供，业务代码不该自己堆文件头解析。
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    /**
     * 允许的图片后缀。这里必须是「集合精确匹配」：
     */
    private static final List<String> ALLOWED_SUFFIX = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    // 上传目录：读 application.yml 的 upload.path，默认是用户主目录下的 campus-activity-uploads/
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

        // 后缀白名单：不在名单里直接拒绝（suffix 已转小写且含点）
        String suffix = suffixOf(file.getOriginalFilename());
        if (!ALLOWED_SUFFIX.contains(suffix)) {
            return Result.error("只支持 jpg/jpeg/png/gif/webp 图片");
        }

        // 用 UUID 重命名，避免不同用户传同名文件互相覆盖
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 确保目录存在（第一次上传时自动创建）
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

    /** 取小写后缀（含点）；没有后缀返回空串，交给白名单去拒绝 */
    private String suffixOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf(".");
        return (dot < 0) ? "" : originalName.substring(dot).toLowerCase();
    }
}