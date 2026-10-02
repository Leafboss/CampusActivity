package com.example.campus_activity.service.impl;

import com.example.campus_activity.mapper.ActivityMapper;
import com.example.campus_activity.pojo.Activity;
import com.example.campus_activity.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * 活动业务实现：本项目业务很轻，大部分只是对 mapper 的一层封装。
 * 保留 service 层是为了分层规范——业务规则（默认图片、删除图片文件）都收在这里，
 * 以后加规则（如时间校验、状态自动计算）也只改这里。
 */
@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    /**
     * 没传图片时用的默认占位图（前端打包的静态资源，不在后端磁盘上）。
     * 用中性的「待补充活动图片」，而不是 activity-1.jpg ——
     * 后者是种子数据里「迎新晚会」那个活动的配图，拿来当默认值会让新建的活动
     * 顶着别人的标题，语义上就不对。
     */
    private static final String DEFAULT_IMAGE = "/images/placeholder.jpg";

    /** 用户上传图片的访问前缀：数据库里存 /uploads/xxx.png，物理文件在 upload.path 目录下 */
    private static final String UPLOAD_URL_PREFIX = "/uploads/";

    /** 上传目录：与 FileUploadController、WebConfig 读的是同一份配置（upload.path） */
    @Value("${upload.path:uploads/}")
    private String uploadPath;

    @Autowired
    private ActivityMapper activityMapper;

    @Override
    public List<Activity> list(String keyword, Integer status) {
        return activityMapper.findByCondition(normalizeKeyword(keyword), status);
    }

    /**
     * 把「空串 / 只有空格」的关键词统一成 null。
     * 这样 XML 的动态 SQL 里就只需判断一种情况（!= null）——前端清空搜索框时传的往往是空串，
     * 如果在 SQL 层再判断一次 keyword != ''，条件就会两边都要维护。
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public Activity getById(Long id) {
        return activityMapper.findById(id);
    }

    @Override
    public void add(Activity activity) {
        activity.setImage(fallbackImage(activity.getImage()));
        activityMapper.insert(activity);
    }

    @Override
    public void update(Activity activity) {
        // 1) 先查旧记录，把原来用的图片记下来（用于「换图后清理旧文件」）
        Activity old = activityMapper.findById(activity.getId());

        // 2) 空图片回退默认占位图：与 add() 同一条规则。
        //    否则前端点了「移除」保存后，库里会存进空串，页面就是一个空 src 的裂图。
        activity.setImage(fallbackImage(activity.getImage()));

        // 3) 更新数据库
        activityMapper.update(activity);

        // 4) 换过图就把旧的上传图从磁盘删掉。
        //    必须放在更新之后：deleteUploadedFile 里的「引用计数 = 0 才删」要基于更新后的数据判定。
        if (old != null && !activity.getImage().equals(old.getImage())) {
            deleteUploadedFile(old.getImage());
        }
    }

    @Override
    public void delete(Long id) {
        // 1) 先查记录，把它的图片路径留下来（记录删完就查不到了）
        Activity activity = activityMapper.findById(id);
        if (activity == null) {
            return; // 记录本来就不存在，直接结束
        }

        // 2) 删数据库里的记录
        activityMapper.deleteById(id);

        // 3) 再删磁盘上的图片文件。
        //    顺序必须是「先删库、后删文件」：万一反过来且删库失败，
        //    结果就是记录还在、图片没了——列表页上直接一张裂图。
        deleteUploadedFile(activity.getImage());
    }

    /** 图片路径为空时回退成默认占位图（新增、修改共用同一条规则） */
    private String fallbackImage(String image) {
        return (image == null || image.isEmpty()) ? DEFAULT_IMAGE : image;
    }

    /**
     * 清理活动图片在磁盘上的文件。
     *
     * 设计要点：
     * 1) 只处理 /uploads/ 开头的「用户上传图」；/images/ 开头的是前端打包进去的占位图，
     *    压根不在后端磁盘上，必须放过，否则会误删前端静态资源。
     * 2) 属于 best-effort：文件删不掉只记日志，不影响接口返回成功——
     *    数据库记录已经没了，留个孤儿文件不影响业务。
     * 3) 删除前做两道保险：文件名形态校验（防路径穿越）+ 检查是否还有别的活动在引用同一张图。
     */
    private void deleteUploadedFile(String image) {
        if (image == null || !image.startsWith(UPLOAD_URL_PREFIX)) {
            return; // 占位图或空值，无需处理
        }

        String fileName = image.substring(UPLOAD_URL_PREFIX.length());
        // 保险一：只接受「普通文件名」，挡掉 ../ 这类路径穿越（数据库被手工改脏时的兜底）
        if (fileName.isEmpty() || fileName.contains("..") || !fileName.matches("[A-Za-z0-9._-]+")) {
            log.warn("图片路径不合法，跳过删除：{}", image);
            return;
        }

        // 保险二：同一张图可能被别的活动也引用着，确认没人用了才删
        if (activityMapper.countByImage(image) > 0) {
            log.info("图片仍被其他活动引用，保留文件：{}", image);
            return;
        }

        File file = new File(new File(uploadPath).getAbsolutePath(), fileName);
        if (file.exists() && file.delete()) {
            log.info("已删除活动图片：{}", file.getAbsolutePath());
        } else {
            log.warn("图片文件不存在或删除失败（不影响本次删除）：{}", file.getAbsolutePath());
        }
    }
}