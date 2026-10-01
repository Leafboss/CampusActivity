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
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传接口：接收前端传来的图片，存到本地 uploads/ 目录。
 * 数据库只存相对路径（如 /uploads/xxx.jpg），不存文件本体。
 *
 * 为什么要有两道类型判断：getOriginalFilename() 是「客户端自己声明」的字符串，和文件真实内容
 * 没有任何关系 —— 任何文件改名成 photo.png 都能骗过单纯的后缀判断。所以：
 *   第一道（后缀白名单）：快速挡掉明显不是图片的文件名；
 *   第二道（魔数嗅探）：读文件头字节看内容到底是不是图片，并用嗅探结果决定落盘后缀。
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    /**
     * 允许的图片后缀。这里必须是「集合精确匹配」：
     * 原写法 "...".contains(suffix) 是子串判断，".jp"、".p"、".g" 这类后缀也能蒙混过关。
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

        // 第一道：后缀白名单（只是预筛，不能当结论 —— 文件名是客户端说了算的）
        String suffix = suffixOf(file.getOriginalFilename());
        if (!ALLOWED_SUFFIX.contains(suffix)) {
            return Result.error("只支持 jpg/jpeg/png/gif/webp 图片");
        }

        // 第二道：读文件头做魔数嗅探，用「内容」判断真实类型
        String realType;
        try {
            realType = sniffImageType(file);
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            return Result.error("读取文件失败：" + e.getMessage());
        }
        if (realType == null) {
            log.warn("文件内容不是图片，已拒绝：{}", file.getOriginalFilename());
            return Result.error("文件内容不是有效图片（改后缀名是没用的）");
        }

        // 用 UUID 重命名，避免不同用户传同名文件互相覆盖；后缀取嗅探出来的真实类型
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + realType;

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

    /**
     * 魔数（magic number）嗅探：读文件开头 12 个字节，比对常见图片格式固定的「内容指纹」。
     * 返回 jpg / png / gif / webp；认不出来说明不是这几类图片，返回 null。
     *
     * 各格式的文件头：jpg = FF D8 FF、png = 89 50 4E 47 0D 0A 1A 0A、
     * gif = "GIF8"(47 49 46 38)、webp = "RIFF" 开头且第 9-12 字节是 "WEBP"。
     */
    private String sniffImageType(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(12);

            if (startsWith(head, 0xFF, 0xD8, 0xFF)) {
                return "jpg";
            }
            if (startsWith(head, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
                return "png";
            }
            if (startsWith(head, 0x47, 0x49, 0x46, 0x38)) {
                return "gif";
            }
            if (head.length >= 12 && startsWith(head, 0x52, 0x49, 0x46, 0x46)
                    && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P') {
                return "webp";
            }
        }
        return null;
    }

    /** head 是否以给定的这些字节开头（magic 里写的是无符号值，所以要 & 0xFF 再比） */
    private boolean startsWith(byte[] head, int... magic) {
        if (head.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if ((head[i] & 0xFF) != magic[i]) {
                return false;
            }
        }
        return true;
    }
}