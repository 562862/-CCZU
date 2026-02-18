# cczu-无心

**常州大学竞赛信息聚合平台** — 自动爬取多学院及校级职能部门的竞赛公告，提供搜索、筛选、邮件通知功能。

> 双击即用，无需安装 Java、数据库等任何依赖。

## 功能

- **多源爬虫** — 覆盖 25 个二级学院 + 4 个校级部门，共 29 个信息源
- **智能识别** — 33 个竞赛关键词匹配，自动分类（获奖喜报/报名通知/竞赛公告/其他通知）
- **级别检测** — 根据标题自动标注国赛/省赛/校赛
- **深度提取** — 从详情页正则提取截止日期、组织单位
- **多维筛选** — 按学院、分类、级别、日期范围组合检索
- **定时更新** — 每 15 分钟自动抓取新公告
- **邮件通知** — 发现新竞赛自动发送邮件（可选配置）
- **赛博朋克 UI** — 深色主题，霓虹网格动画背景

## 快速开始

### 下载运行（推荐）

前往 [Releases](https://github.com/562862/-CCZU/releases/latest) 下载：

| 文件 | 说明 |
|------|------|
| `CCZU-WuXin 安装包` | Windows 安装程序，含桌面快捷方式 |
| `CCZU-WuXin 便携版` | 解压即用，无需安装 |

启动后浏览器自动打开 `http://localhost:8888`，爬虫立即开始工作。

### 源码运行

```bash
git clone https://github.com/562862/-CCZU.git
cd -CCZU
mvn spring-boot:run -DskipTests
```

默认使用 H2 内嵌数据库，零配置启动。如需 MySQL：

```bash
mysql -u root -p your_db < sql/migrate_v1.2.sql
mvn spring-boot:run -DskipTests -Dspring-boot.run.profiles=mysql
```

## 数据源

- 当前覆盖 **25 个二级学院 + 4 个校级部门**，共 **29 个信息源**。
- 完整配置清单见 `src/main/resources/application.yml` 的 `app.crawler.sources`。

## 邮件通知（可选）

默认关闭。如需启用，在用户目录创建配置文件 `~/.cczu-wuxin/config.yml`：

```yaml
app:
  mail:
    enabled: true
    notify-to: your_email@example.com

spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: your_email@qq.com
    password: your_smtp_auth_code  # SMTP 授权码，非登录密码
    properties:
      mail.smtp.ssl.enable: true
      mail.smtp.auth: true
```

项目中 `config-template.yml` 有完整模板。

## 技术栈

| 组件 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 + MyBatis |
| 数据库 | H2 内嵌（默认） / MySQL 8.0（可选） |
| 爬虫 | Jsoup 1.17.2 |
| 前端 | 原生 HTML/CSS/JS |
| 打包 | jpackage（内嵌 JRE） |

## 项目结构

```
├── pom.xml
├── build-installer.bat          # 构建安装包脚本
├── config-template.yml          # 邮件配置模板
├── sql/                         # MySQL 迁移脚本
├── src/main/java/.../
│   ├── CczuWuxinApplication.java
│   ├── controller/              # REST API
│   ├── service/
│   │   ├── CrawlerService.java  # 核心爬虫逻辑
│   │   ├── EmailService.java    # 邮件通知
│   │   └── CompetitionService.java
│   ├── mapper/                  # MyBatis 数据访问
│   └── task/CrawlerTask.java    # 定时调度
└── src/main/resources/
    ├── application.yml          # H2 默认配置
    ├── application-mysql.yml    # MySQL 开发配置
    ├── schema.sql               # 自动建表
    └── static/                  # 前端页面
```

## API

| 端点 | 说明 |
|------|------|
| `GET /api/competitions` | 竞赛列表（支持 keyword、college、category、level、startDate、endDate、page、size） |
| `GET /api/competitions/all` | 全量竞赛数据（一次性返回所有数据，供客户端软件拉取） |
| `GET /api/competitions/{id}` | 竞赛详情 |
| `GET /api/colleges` | 学院列表 |
| `GET /api/categories` | 分类列表 |
| `GET /api/levels` | 级别列表 |

### GET /api/competitions/all

一次性返回所有竞赛数据，按发布日期倒序排列，无需分页参数。适用于客户端软件全量拉取场景。

**请求示例：**

```
GET http://localhost:8888/api/competitions/all
```

**响应示例：**

```json
[
  {
    "id": 67,
    "title": "关于选拔推荐我校教师参加第六届江苏省高校青年教师教学竞赛的预通知",
    "url": "https://jwc.cczu.edu.cn/2026/0202/c1425a409564/page.htm",
    "content": null,
    "publishDate": "2026-02-08",
    "crawlTime": "2026-02-15T11:36:28.818574",
    "college": "教务处",
    "category": "报名通知",
    "level": "省赛",
    "deadline": "2026-04-12",
    "organizer": null
  }
]
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | number | 竞赛 ID |
| `title` | string | 竞赛标题 |
| `url` | string | 原文链接 |
| `content` | string/null | 详情页正文（列表接口不返回，详情接口返回） |
| `publishDate` | string | 发布日期（yyyy-MM-dd） |
| `crawlTime` | string | 爬取时间（ISO 8601） |
| `college` | string | 来源学院/部门 |
| `category` | string | 分类（获奖喜报/报名通知/竞赛公告/其他通知） |
| `level` | string/null | 级别（国赛/省赛/校赛） |
| `deadline` | string/null | 截止日期（yyyy-MM-dd） |
| `organizer` | string/null | 组织单位 |

## 从源码构建安装包

前置条件：JDK 17+、Maven 3.6+、WiX Toolset 3（可选，用于生成 exe 安装包）。

```bash
build-installer.bat
```

脚本自动检测环境、编译打包、生成安装包。产物输出到 `build/output/`。
