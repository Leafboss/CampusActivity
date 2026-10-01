-- ============================================================
-- 校园活动信息管理 · 初始化脚本
-- 用法：mysql -u root -p < init.sql（首次建库建表 + 导入种子数据）
-- 注意：会删除同名旧库重建，仅用于首次初始化/重置练习数据。
--      已有数据的库不要跑这个脚本（会丢数据），要升级表结构请用同目录的 migrate-v2-status-time.sql
-- ============================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS campus_activity;
CREATE DATABASE campus_activity DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE campus_activity;

-- 活动表：字段与前端展示一一对应
CREATE TABLE activity (
    id       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name     VARCHAR(100) NOT NULL              COMMENT '活动名称',
    time     DATETIME     NOT NULL              COMMENT '活动时间（真正的日期时间类型，不是字符串）',
    location VARCHAR(100) NOT NULL              COMMENT '活动地点',
    summary  VARCHAR(255) NOT NULL              COMMENT '一句话简介（列表卡片展示）',
    detail   TEXT                               COMMENT '详细介绍（详情页展示）',
    image    VARCHAR(255)                       COMMENT '图片路径（内置占位图 /images/xxx.jpg，上传图 /uploads/xxx.png）',
    status   TINYINT      NOT NULL DEFAULT 1    COMMENT '状态：1=报名中 / 0=已结束'
) COMMENT '校园活动表';

-- 种子数据：与前端列表页一一对应
INSERT INTO activity (name, time, location, summary, detail, image, status) VALUES
('2026 迎新晚会', '2026-10-15 19:00', '学校大礼堂',
 '歌舞、乐队与抽奖，欢迎新同学加入大家庭。',
 '一年一度的迎新晚会将在学校大礼堂举行。节目涵盖流行乐队、街舞、民乐与话剧社专场，现场还有新生抽奖环节。入场免费，凭校园卡签到，座位先到先得。欢迎所有新老同学前来观看，一起开启新学年。',
 '/images/activity-1.jpg', 1),
('校园篮球联赛·秋季赛', '2026-10-20 15:00', '东区篮球场',
 '以学院为单位组队，争夺秋季赛冠军奖杯。',
 '秋季篮球联赛面向全校各学院报名，每队 5-12 人。小组赛在东区篮球场进行，决赛移师体育馆。报名请联系各学院体育部，截止日期 10 月 12 日。现场设观众席，欢迎同学到场助威。',
 '/images/activity-2.jpg', 1),
('前端技术分享会', '2026-10-22 14:30', '图书馆报告厅',
 '聊聊 Vue 3、工程化与 AI 辅助开发实践。',
 '技术部同学带来前端专题分享：Vue 3 组合式 API 实战、Vite 工程化配置、以及如何用 AI 工具高效完成课程项目。分享后设自由提问环节，适合对 Web 开发感兴趣的同学，零基础也可参加。',
 '/images/activity-3.jpg', 1),
('校运会志愿者招募', '2026-10-08 09:00', '学生事务中心',
 '校运会需要检录、引导、医疗协助等岗位志愿者。',
 '第十一届校运会将于 11 月初举行，现面向全校招募志愿者。岗位包括检录引导、成绩记录、医疗协助与摄影宣传。志愿服务时长可计入第二课堂学分，报名请前往学生事务中心一楼登记。',
 '/images/activity-4.jpg', 1),
('秋季读书分享会', '2026-09-26 19:00', '咖啡店·校园店',
 '本期主题：科幻小说里的未来社会。',
 '读书协会秋季第一期分享会，主题为"科幻小说里的未来社会"，从《三体》聊到《克拉拉与太阳》。每位参与者可带一本想推荐的书。活动已圆满结束，感谢到场同学。',
 '/images/activity-5.jpg', 0),
('校园歌手大赛·海选', '2026-09-20 18:30', '大学生活动中心',
 '海选阶段已结束，复赛名单请关注公众号。',
 '校园歌手大赛海选共吸引 120 余名同学报名，曲风涵盖流行、民谣与说唱。评委由音乐学院老师与往届冠军担任。海选已结束，复赛名单与赛程将在学生会公众号公布。',
 '/images/activity-6.jpg', 0);
