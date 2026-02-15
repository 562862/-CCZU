-- H2 兼容建表脚本（MODE=MySQL 模式下执行）
CREATE TABLE IF NOT EXISTS competition (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(500)  NOT NULL,
    url         VARCHAR(500)  NOT NULL UNIQUE,
    content     CLOB,
    publish_date DATE,
    crawl_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    college     VARCHAR(100),
    category    VARCHAR(50),
    level       VARCHAR(20),
    deadline    DATE,
    organizer   VARCHAR(200)
);

CREATE INDEX IF NOT EXISTS idx_title        ON competition(title);
CREATE INDEX IF NOT EXISTS idx_publish_date ON competition(publish_date);
CREATE INDEX IF NOT EXISTS idx_college      ON competition(college);
CREATE INDEX IF NOT EXISTS idx_category     ON competition(category);
CREATE INDEX IF NOT EXISTS idx_level        ON competition(level);
CREATE INDEX IF NOT EXISTS idx_deadline     ON competition(deadline);
