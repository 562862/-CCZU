# cczu-无心 · 产品需求文档（PRD）

> 常州大学竞赛信息聚合平台 — 本地可部署版本
> 版本：v1.1.0 | 日期：2026-02-14

---

## 1. 产品概述

### 1.1 产品名称
cczu-无心（常州大学竞赛信息聚合网站）

### 1.2 产品描述
一个面向常州大学学生的竞赛信息聚合平台。系统自动定时爬取学校多个学院官网的竞赛相关公告，经关键词过滤与去重后存入本地数据库，按学院和分类专栏组织，并通过 Web 界面提供搜索、筛选、分页浏览功能，同时支持 QQ 邮箱新公告通知。

### 1.3 目标用户
- 常州大学在校学生（尤其是关注学科竞赛的同学）
- 院系辅导员、竞赛指导教师

### 1.4 核心价值
- **信息聚合**：自动抓取多个学院官网的竞赛相关公告，集中展示
- **多维筛选**：按学院、分类专栏、关键词、日期范围组合筛选
- **实时推送**：发现新公告即刻邮件通知，不错过报名时间
- **高效检索**：关键词 + 日期范围组合筛选，快速定位目标公告
- **零依赖前端**：原生 HTML/CSS/JS，无需 Node 构建，开箱即用

---

## 2. 技术方案

| 层 | 选型 | 说明 |
|------|------|------|
| 前端 | 原生 HTML/CSS/JS | 不引入框架，简洁直接 |
| 后端 | Spring Boot 3.2.5 | Java 生态主流，资料丰富 |
| 数据库 | MySQL 8.0+ | 存储公告数据 |
| 爬虫 | Jsoup 1.17.2 | Java HTML 解析库，在 Spring Boot 内用定时任务抓取 |
| 邮件 | Spring Boot Mail | 发送 QQ 邮箱通知 |
| 定时任务 | @Scheduled | 每 15 分钟轮询一次官网 |
| ORM | MyBatis 3.0.3 | SQL 映射与数据访问 |
| 构建 | Maven | 依赖管理与项目构建 |
| JDK | Java 17 | LTS 版本 |

---

## 3. 功能需求

### 3.1 爬虫模块
**优先级：P0**

| 功能编号 | 功能名称 | 功能描述 |
|---------|---------|---------|
| C1.1 | 多源多页爬取 | 遍历配置的多个学院公告源，每个源爬取指定页数 |
| C1.2 | 扩展关键词过滤 | 保留标题含"竞赛/比赛/大赛/竞技/挑战赛/报名/参赛/选拔/评审/答辩/获奖/喜报"的公告 |
| C1.3 | URL 去重 | 入库前检查 URL 是否已存在，避免重复 |
| C1.4 | 详情抓取 | 进入公告详情页，提取正文内容（div.wp_articlecontent） |
| C1.5 | 日期解析 | 支持 yyyy-MM-dd / yyyy/MM/dd / yyyy.MM.dd 多种格式 |
| C1.6 | 定时调度 | 应用启动时立即执行一次，之后每 15 分钟自动执行 |
| C1.7 | 异常容错 | 单条公告抓取失败不影响整体流程，记录日志继续 |
| C1.8 | 学院标记 | 每条公告自动标记来源学院 |
| C1.9 | 分类检测 | 根据标题关键词自动分类：竞赛公告、报名通知、获奖喜报、其他通知 |

**爬取目标**（v1.1 多源）：
| 学院 | URL |
|------|-----|
| 微电子学院 | https://jsjx.cczu.edu.cn/21177/list.htm |
| 材料学院 | https://clxy.cczu.edu.cn/2502/list.htm |
| 石化学院 | https://che.cczu.edu.cn/2968/list.htm |
| 环境学院 | https://cmee.cczu.edu.cn/21936/list.htm |
| 大数据学院 | https://bigdata.cczu.edu.cn/zygg/list.htm |
| 法学院 | https://zfxy.cczu.edu.cn/tzgg/list.htm |

### 3.2 邮件通知模块
**优先级：P1**

| 功能编号 | 功能名称 | 功能描述 |
|---------|---------|---------|
| E1.1 | 新公告通知 | 每次爬取发现新公告后，汇总发送一封 HTML 邮件 |
| E1.2 | 可配置开关 | 通过 application.yml 中 `app.mail.enabled` 控制是否启用 |
| E1.3 | QQ 邮箱 SMTP | 使用 QQ 邮箱 SMTP（smtp.qq.com:465 SSL）发送 |

### 3.3 公告查询模块
**优先级：P0**

| 功能编号 | 功能名称 | 功能描述 |
|---------|---------|---------|
| Q1.1 | 分页列表 | GET /api/competitions，支持 page + size 分页 |
| Q1.2 | 关键词搜索 | keyword 参数，对标题进行 LIKE 模糊匹配 |
| Q1.3 | 日期范围筛选 | startDate / endDate 参数，按发布日期区间过滤 |
| Q1.4 | 学院筛选 | college 参数，按学院过滤 |
| Q1.5 | 分类筛选 | category 参数，按分类专栏过滤 |
| Q1.6 | 详情查询 | GET /api/competitions/{id}，返回单条公告完整信息 |
| Q1.7 | 组合查询 | 关键词 + 日期 + 学院 + 分类可任意组合，均为可选参数 |
| Q1.8 | 学院列表 | GET /api/colleges，返回去重学院列表 |
| Q1.9 | 分类列表 | GET /api/categories，返回去重分类列表 |

### 3.4 前端展示模块
**优先级：P0**

| 功能编号 | 功能名称 | 功能描述 |
|---------|---------|---------|
| F1.1 | 主页列表 | 卡片式展示公告标题、发布日期，支持分页 |
| F1.2 | 搜索栏 | 关键词输入 + 学院下拉 + 分类下拉 + 日期范围选择 + 检索按钮 |
| F1.3 | 详情跳转 | 点击卡片自动跳转至官网原文链接 |
| F1.4 | 统计展示 | 头部显示公告总数、刷新频率 |
| F1.5 | 赛博朋克主题 | 深色科技风 UI，cyan 荧光色调，网格动画背景 |
| F1.6 | 响应式布局 | 适配桌面端与移动端 |
| F1.7 | 学院/分类标签 | 卡片中显示学院 badge（cyan）和分类 badge（purple） |

---

## 4. 数据库设计

**数据库名**：`cczu_wuxin`（UTF8MB4 编码）

### competition 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| title | VARCHAR(500) | NOT NULL | 公告标题 |
| url | VARCHAR(500) | NOT NULL, UNIQUE | 原文链接（去重依据） |
| content | TEXT | 可空 | 公告详情正文 |
| publish_date | DATE | 可空 | 发布日期 |
| crawl_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 抓取时间 |
| college | VARCHAR(100) | 可空 | 来源学院 |
| category | VARCHAR(50) | 可空 | 分类专栏 |

**索引**：
- `idx_publish_date` — 发布日期索引，加速日期范围查询
- `idx_title` — 标题前缀索引（255 字符），加速关键词搜索
- `idx_college` — 学院索引，加速学院筛选
- `idx_category` — 分类索引，加速分类筛选

**分类枚举**（代码硬编码）：竞赛公告、报名通知、获奖喜报、其他通知

---

## 5. API 接口设计

### 5.1 公告列表
```
GET /api/competitions

参数（均可选）：
  keyword    string   搜索关键词（标题模糊匹配）
  startDate  string   开始日期（yyyy-MM-dd）
  endDate    string   结束日期（yyyy-MM-dd）
  college    string   学院名称（精确匹配）
  category   string   分类专栏（精确匹配）
  page       int      页码（默认 1）
  size       int      每页数量（默认 10）

响应：
{
  "list": [ { id, title, url, content, publishDate, crawlTime, college, category } ],
  "total": 100,
  "page": 1,
  "size": 10,
  "totalPages": 10
}
```

### 5.2 公告详情
```
GET /api/competitions/{id}

响应：
{
  "id": 1,
  "title": "...",
  "url": "https://...",
  "content": "...",
  "publishDate": "2026-02-14",
  "crawlTime": "2026-02-14T10:30:00",
  "college": "微电子学院",
  "category": "竞赛公告"
}
```

### 5.3 学院列表
```
GET /api/colleges

响应：
[ "微电子学院", "材料学院", "石化学院", ... ]
```

### 5.4 分类列表
```
GET /api/categories

响应：
[ "竞赛公告", "报名通知", "获奖喜报", "其他通知" ]
```

---

## 6. 项目结构

```
cczu-无心/
├── pom.xml                          # Maven 配置
├── sql/
│   ├── init.sql                     # 数据库初始化脚本
│   └── migrate_v1.1.sql             # v1.1 迁移脚本（新增 college/category）
├── src/main/
│   ├── java/com/cczu/wuxin/
│   │   ├── CczuWuxinApplication.java    # 启动类
│   │   ├── config/
│   │   │   └── WebMvcConfig.java        # CORS 配置
│   │   ├── controller/
│   │   │   └── CompetitionController.java
│   │   ├── entity/
│   │   │   └── Competition.java
│   │   ├── mapper/
│   │   │   └── CompetitionMapper.java
│   │   ├── service/
│   │   │   ├── CompetitionService.java
│   │   │   ├── CrawlerProperties.java   # 多源爬虫配置属性
│   │   │   ├── CrawlerService.java
│   │   │   └── EmailService.java
│   │   └── task/
│   │       └── CrawlerTask.java
│   └── resources/
│       ├── application.yml              # 应用配置
│       ├── mapper/
│       │   └── CompetitionMapper.xml    # MyBatis SQL 映射
│       └── static/
│           ├── index.html               # 主页
│           ├── detail.html              # 详情页
│           ├── css/style.css            # 样式
│           └── js/
│               ├── index.js             # 主页逻辑
│               └── detail.js            # 详情页逻辑
└── PRD.md                           # 本文档
```

---

## 7. 本地部署指南

### 7.1 环境要求

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 推荐 OpenJDK 17 或 Oracle JDK 17 |
| Maven | 3.8+ | 项目构建工具 |
| MySQL | 8.0+ | 数据存储 |

### 7.2 部署步骤

**第一步：初始化数据库**
```bash
mysql -u root -p < sql/init.sql

# 如果从 v1.0 升级，执行迁移脚本
mysql -u root -p < sql/migrate_v1.1.sql
```

**第二步：修改配置**

编辑 `src/main/resources/application.yml`，按实际情况修改：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cczu_wuxin?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: root          # ← 改为你的 MySQL 用户名
    password: 123456        # ← 改为你的 MySQL 密码

  mail:
    username: xxx@qq.com    # ← 改为你的 QQ 邮箱
    password: xxx           # ← 改为 QQ 邮箱授权码（非登录密码）

app:
  mail:
    enabled: true           # 不需要邮件通知可改为 false
    notify-to: xxx@qq.com   # ← 接收通知的邮箱
```

**第三步：构建并运行**
```bash
# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/cczu-wuxin-1.0.0.jar
```

**第四步：访问**

浏览器打开 `http://localhost:8888`

### 7.3 配置项说明

| 配置路径 | 默认值 | 说明 |
|---------|--------|------|
| server.port | 8888 | 服务端口 |
| app.crawler.sources | 6 个学院 | 多源爬虫配置列表 |
| app.crawler.sources[].college | - | 学院名称 |
| app.crawler.sources[].base-url | - | 爬取目标 URL |
| app.crawler.sources[].pages | 3 | 每个源爬取页数 |
| app.crawler.sources[].selector | li.news | 列表项 CSS 选择器 |
| app.mail.enabled | true | 邮件通知开关 |

---

## 8. 非功能性需求

### 8.1 性能
- 单次爬取 5 页，耗时控制在 30 秒内
- API 查询响应时间 < 200ms（千条数据量级）

### 8.2 可靠性
- 爬虫单条失败不影响整体，异常记录日志
- 数据库 URL 唯一约束保证不重复入库

### 8.3 安全性
- 前端 HTML 转义防 XSS
- CORS 配置限制跨域来源
- 数据库密码、邮箱授权码不硬编码到代码中（通过配置文件管理）

### 8.4 可维护性
- 标准 Spring Boot 分层架构（Controller → Service → Mapper）
- MyBatis XML 管理 SQL，便于调整查询逻辑
- 配置外置，修改无需重新编译

---

## 9. 实现进度

| 模块 | 状态 | 说明 |
|------|------|------|
| 数据库设计 | ✅ 已完成 | init.sql + migrate_v1.1.sql |
| 爬虫模块 | ✅ 已完成 | 多源多页爬取 + 扩展关键词 + 去重 + 详情抓取 + 分类检测 |
| 邮件通知 | ✅ 已完成 | QQ 邮箱 HTML 通知 |
| 查询 API | ✅ 已完成 | 分页 + 关键词 + 日期 + 学院 + 分类 |
| 前端主页 | ✅ 已完成 | 赛博朋克主题 + 学院/分类下拉筛选 + 标签展示 |
| 前端详情页 | ✅ 已完成 | 自动跳转原文 |
| 多学院支持 | ✅ 已完成 | 6 个学院公告源，可配置扩展 |
| 分类专栏 | ✅ 已完成 | 自动分类：竞赛公告/报名通知/获奖喜报/其他通知 |
| 本地部署 | ✅ 已完成 | Maven 打包 + java -jar 启动 |

---

## 10. 未来规划

### 10.1 短期优化
- [x] 支持更多学院/网站的公告源（v1.1 已实现 6 个学院）
- [x] 公告分类标签（v1.1 已实现自动分类）
- [ ] 添加公告收藏/标记功能
- [ ] 支持微信/钉钉 Webhook 通知

### 10.2 中期扩展
- [ ] 用户注册登录，个性化关键词订阅
- [ ] Docker 一键部署

### 10.3 长期规划
- [ ] 多校聚合（支持接入其他高校公告源）
- [ ] 移动端小程序
- [ ] AI 摘要生成（自动提取报名时间、参赛要求等关键信息）

---

## 11. 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|---------|
| v1.0.0 | 2026-02-14 | 初始版本，完成全部核心功能与本地部署支持 |
| v1.1.0 | 2026-02-14 | 多学院支持（6 个学院）、分类专栏、扩展关键词、学院/分类筛选 |
