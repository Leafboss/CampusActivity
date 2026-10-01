-- ============================================================
-- 迁移脚本 v1 -> v2（2026-10-01 编写，2026-10-02 补充体检项）
-- 作用：把 activity 表升级成「status 用 0/1 的 TINYINT、time 用真正的 DATETIME」
-- 适用：已经有数据的库。
--      不要用 init.sql 升级 —— 它开头是 DROP DATABASE，跑一次数据全没，会丢活动数据和图片关联。
-- 用法（Navicat 或命令行都一样）：整段按顺序执行；第 0/1 步是"看结果"的查询，第 2/3 步才动表结构。
-- 命令行：mysql -u root -p campus_activity < migrate-v2-status-time.sql
-- ============================================================

SET NAMES utf8mb4;
USE campus_activity;

-- ---------- 第 0 步：备份 ----------
-- 只备份数据，不含索引/自增等结构信息，够用来"改错了拿回数据"。
-- 迁移顺利的话，验收无误后可以自己把这张备份表删掉。
CREATE TABLE activity_bak_20261002 AS SELECT * FROM activity;

-- ---------- 第 1 步：体检（必须都过了再往下走）----------
-- 1a) 时间列有没有"不是日期时间"的脏数据（比如手打的「明天下午」）。
--     这条必须返回 0 行；否则改 DATETIME 会失败，或在非严格模式下被塞成 0000-00-00。
--     允许出现 "2026-10-15 19:00"、"2026-10-15 19:00:30"、"2026-10-15T19:00"（第 3 步会把 T 换成空格）。
SELECT id, name, time FROM activity
WHERE time NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}[ T][0-9]{2}:[0-9]{2}(:[0-9]{2})?$';

-- 1b) 状态列都有哪些值（期望只看到 upcoming / ended，出现别的值要人工确认怎么翻译）
SELECT status, COUNT(*) AS cnt FROM activity GROUP BY status;

-- ---------- 第 2 步：status 由字符串换成 0/1 ----------
-- 先把值就地翻译成 '1' / '0'（此刻列还是 VARCHAR，存字符串没问题），
-- 再把列类型改成 TINYINT，MySQL 会自动把 '1' 转成数字 1。
-- 认不出来的值原样保留 → 下一步 ALTER 会报错提醒，不会静默变成 0。
UPDATE activity
SET status = CASE
    WHEN status = 'upcoming' THEN '1'
    WHEN status = 'ended'    THEN '0'
    ELSE status              -- 含已经改过的 '0'/'1'，重复执行也安全
END;

-- 改类型前再确认一眼：期望只剩 1 和 0
SELECT id, name, status FROM activity ORDER BY id;

ALTER TABLE activity
    MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=报名中 / 0=已结束';

-- ---------- 第 3 步：time 由字符串换成 DATETIME ----------
-- 先兜掉可能存在的 'T' 分隔写法（2026-10-15T19:00），再改列类型。
UPDATE activity SET time = REPLACE(time, 'T', ' ');
ALTER TABLE activity
    MODIFY COLUMN time DATETIME NOT NULL COMMENT '活动时间（真正的日期时间类型，不是字符串）';

-- ---------- 第 4 步：验收 ----------
-- 期望：status 是 tinyint、time 是 datetime
SHOW COLUMNS FROM activity LIKE 'status';
SHOW COLUMNS FROM activity LIKE 'time';
-- 期望：状态只有 0 / 1，时间是正常的日期时间，且顺序按时间倒序
SELECT id, name, time, status FROM activity ORDER BY time DESC;