package com.example.campus_activity.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动实体类：与数据库 activity 表一一对应（字段也直接对应前端展示需要）。
 *
 * 字段上的校验注解只在「本类作为接口入参」时生效（Controller 上标了 @Valid）：
 * 目的是让非法数据在进 Service / SQL 之前就被拦下来，并且能告诉前端到底哪个字段不合规。
 * 各字段的 @Size 上限是对着 init.sql 的列宽写的，两边要一起改。
 */
@Data
public class Activity {

    /** 主键，自增；新增不用传，插入后由数据库回填 */
    private Long id;

    /** 活动名称（数据库 VARCHAR(100)） */
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称不能超过 100 个字")
    private String name;

    /**
     * 活动时间：Java 侧是 LocalDateTime、数据库里是 DATETIME（真正的日期时间类型，不是字符串）。
     * @JsonFormat 把接口的入参 / 出参格式统一固定成 "2026-10-15 19:00"：
     * 格式与改造前前端约定的字符串完全一致，所以前端展示逻辑一行都不用改。
     */
    @NotNull(message = "活动时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime time;

    /** 活动地点（数据库 VARCHAR(100)） */
    @NotBlank(message = "活动地点不能为空")
    @Size(max = 100, message = "活动地点不能超过 100 个字")
    private String location;

    /** 一句话简介（列表卡片上展示），数据库 VARCHAR(255) */
    @NotBlank(message = "活动简介不能为空")
    @Size(max = 255, message = "活动简介不能超过 255 个字")
    private String summary;

    /** 详细介绍（详情页展示），数据库是 TEXT，这里只给一个宽松上限防超大文本 */
    @Size(max = 5000, message = "活动详情不能超过 5000 个字")
    private String detail;

    /**
     * 图片路径：内置占位图是 /images/xxx.jpg，用户上传的是 /uploads/xxx.png，
     * 前端直接当 img 的 src 用；留空的由 Service 回退成默认占位图。
     */
    @Size(max = 255, message = "图片路径不能超过 255 个字")
    private String image;

    /**
     * 状态：1 = 报名中 / 0 = 已结束（数据库 TINYINT，列注释里写着同一份映射）。
     * 用数值是为了省空间、便于比较；可读性由前端 utils/status.js 统一翻译，别处不写字面量。
     */
    @NotNull(message = "活动状态不能为空")
    @Min(value = 0, message = "活动状态只能是 0（已结束）或 1（报名中）")
    @Max(value = 1, message = "活动状态只能是 0（已结束）或 1（报名中）")
    private Integer status;
}