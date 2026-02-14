-- v1.2 深度竞赛数据挖掘扩展
-- 新增字段：比赛级别、截止日期、组织单位

ALTER TABLE competition
  ADD COLUMN level VARCHAR(20) DEFAULT NULL COMMENT '比赛级别（国赛/省赛/校赛）',
  ADD COLUMN deadline DATE DEFAULT NULL COMMENT '截止日期',
  ADD COLUMN organizer VARCHAR(200) DEFAULT NULL COMMENT '组织单位';

ALTER TABLE competition
  ADD INDEX idx_level (level),
  ADD INDEX idx_deadline (deadline);
