-- 竞赛公告表
CREATE DATABASE IF NOT EXISTS cczu_wuxin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cczu_wuxin;

CREATE TABLE IF NOT EXISTS competition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL COMMENT '公告标题',
    url VARCHAR(500) NOT NULL UNIQUE COMMENT '原文链接（用于去重）',
    content TEXT COMMENT '公告详情内容（HTML）',
    publish_date DATE COMMENT '发布日期',
    crawl_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '抓取时间',
    INDEX idx_publish_date (publish_date),
    INDEX idx_title (title(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛公告';
