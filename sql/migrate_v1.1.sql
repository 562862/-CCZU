-- v1.1 多学院 + 分类专栏扩展
-- 新增 college（学院）和 category（分类专栏）字段

USE cczu_wuxin;

ALTER TABLE competition
  ADD COLUMN college VARCHAR(100) DEFAULT NULL COMMENT '学院',
  ADD COLUMN category VARCHAR(50) DEFAULT NULL COMMENT '分类专栏',
  ADD INDEX idx_college (college),
  ADD INDEX idx_category (category);
