package com.example.campus_activity.pojo;

import lombok.Data;

/**
 * 活动实体类：与数据库 activity 表一一对应（字段也直接对应前端展示需要）
 */
@Data
public class Activity {

    /** 主键，自增 */
    private Long id;

    /** 活动名称 */
    private String name;

    /** 活动时间，格式约定为 "2026-10-15 19:00"，前端直接按字符串切分展示 */
    private String time;

    /** 活动地点 */
    private String location;

    /** 一句话简介（列表卡片上展示） */
    private String summary;

    /** 详细介绍（详情页展示） */
    private String detail;

    /** 图片路径（存前端静态资源路径，如 /images/activity-1.jpg；不上传文件，保持简单） */
    private String image;

    /** 状态：upcoming=报名中 / ended=已结束 */
    private String status;
}
