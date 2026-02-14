# cczu-无心 项目状态文档

> 最后更新：2026-02-14 · 当前版本：v1.2.0

## 项目概述

常州大学竞赛信息聚合平台，自动爬取多学院及校级职能部门的竞赛公告，提供搜索、筛选、邮件通知等功能。

## 技术栈

- Spring Boot 2.7.18 + MyBatis 2.3.2 + MySQL 8.0
- Jsoup 1.17.2（HTML 解析/爬虫）
- 前端：原生 HTML/CSS/JS，赛博朋克深色主题
- Java 11（实际运行环境 JDK 25）
- Maven 3.9.12

## 仓库信息

- 远程：`https://github.com/562862/-CCZU.git`
- 分支：`main`
- 本地路径：`C:\Users\27348\Desktop\cczu-无心`

## 版本历史

| 版本 | Commit | 内容 |
|------|--------|------|
| v1.1.0 | 3118a30 | 多学院爬取（6 个二级学院）、分类专栏（获奖喜报/报名通知/竞赛公告/其他通知） |
| v1.2.0 | cecbe2e | 深度竞赛数据挖掘扩展（详见下方） |

## v1.2.0 改动清单

### 新增爬虫源（4 个校级职能部门）

| 部门 | URL | 页面结构 |
|------|-----|---------|
| 教务处 | jwc.cczu.edu.cn/1425/list.htm | 嵌套 table（`tr:has(table)`） |
| 校团委 | tw.cczu.edu.cn/ggtz/list.htm | li 列表（`li:has(a[href*=page])`） |
| 创新创业学院-通知 | cxcy.cczu.edu.cn/6900/list.htm | 嵌套 table |
| 创新创业学院-竞赛 | cxcy.cczu.edu.cn/18291/list.htm | 嵌套 table |

### 关键词库扩展

从 12 个扩展到 33 个，新增具体赛事名：蓝桥杯、数学建模、ACM、电子设计、机械创新、挑战杯、互联网+、智能车、机器人、程序设计、算法、编程、物理实验、化学实验、力学竞赛、结构设计、节能减排、工程训练、创业计划、职业规划

### 新增字段

| 字段 | 类型 | 说明 | 提取方式 |
|------|------|------|---------|
| level | VARCHAR(20) | 比赛级别（国赛/省赛/校赛） | 标题关键词匹配 |
| deadline | DATE | 截止日期 | 详情页正则提取 |
| organizer | VARCHAR(200) | 组织单位 | 详情页正则提取 |

### 前端新增

- LEVEL 下拉筛选（全部级别/国赛/省赛/校赛）
- 级别 badge（国赛=红色、省赛=橙色、校赛=绿色）
- 截止日期显示（⏰ 图标）

### API 变更

- `GET /api/competitions` 新增 `level` 查询参数
- `GET /api/levels` 新增端点，返回去重级别列表

## 当前数据库结构

```sql
CREATE TABLE competition (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(500) NOT NULL,
  url         VARCHAR(500) NOT NULL UNIQUE,
  content     TEXT,
  publish_date DATE,
  crawl_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
  college     VARCHAR(100),    -- v1.1
  category    VARCHAR(50),     -- v1.1
  level       VARCHAR(20),     -- v1.2
  deadline    DATE,            -- v1.2
  organizer   VARCHAR(200),    -- v1.2
  INDEX idx_title (title),
  INDEX idx_publish_date (publish_date),
  INDEX idx_college (college),
  INDEX idx_category (category),
  INDEX idx_level (level),
  INDEX idx_deadline (deadline)
);
```

## 文件结构

```
cczu-无心/
├── pom.xml
├── sql/
│   ├── init.sql                    # 建库建表
│   ├── migrate_v1.1.sql            # v1.1 加 college/category
│   └── migrate_v1.2.sql            # v1.2 加 level/deadline/organizer
├── src/main/java/com/cczu/wuxin/
│   ├── CczuWuxinApplication.java
│   ├── config/WebMvcConfig.java          # CORS
│   ├── controller/CompetitionController.java
│   ├── entity/Competition.java
│   ├── mapper/CompetitionMapper.java
│   ├── service/
│   │   ├── CompetitionService.java
│   │   ├── CrawlerService.java           # 核心爬虫
│   │   ├── CrawlerProperties.java        # 多源配置
│   │   └── EmailService.java             # 邮件通知
│   └── task/CrawlerTask.java             # 定时任务（15 分钟）
├── src/main/resources/
│   ├── application.yml                   # 10 个爬虫源配置
│   ├── mapper/CompetitionMapper.xml
│   └── static/
│       ├── index.html
│       ├── detail.html
│       ├── js/index.js
│       ├── js/detail.js
│       └── css/style.css
```

## 当前爬虫源（共 10 个）

| # | 名称 | college 值 | 来源版本 |
|---|------|-----------|---------|
| 1 | 王诤微电子学院 | 微电子学院 | v1.1 |
| 2 | 材料科学与工程学院 | 材料学院 | v1.1 |
| 3 | 石油化工学院 | 石化学院 | v1.1 |
| 4 | 环境科学与工程学院 | 环境学院 | v1.1 |
| 5 | 阿里云大数据学院 | 大数据学院 | v1.1 |
| 6 | 法学院 | 法学院 | v1.1 |
| 7 | 教务处 | 教务处 | v1.2 |
| 8 | 校团委 | 校团委 | v1.2 |
| 9 | 创新创业学院-通知 | 创新创业学院 | v1.2 |
| 10 | 创新创业学院-竞赛 | 创新创业学院 | v1.2 |

## 运行方式

```bash
# 数据库迁移（首次或版本升级时）
mysql -u root -p123456 cczu_wuxin < sql/migrate_v1.2.sql

# 编译
mvn compile -DskipTests

# 启动（端口 8888）
mvn spring-boot:run -DskipTests
```

## 后续版本可考虑方向

- 更多学院源接入
- 订阅功能（按学院/级别/赛事订阅邮件）
- 数据统计面板（各学院竞赛数量、级别分布图表）
- 详情页内容展示（目前是重定向到原文）
- 移动端适配优化
