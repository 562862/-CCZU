package com.cczu.wuxin.service;

import com.cczu.wuxin.entity.Competition;
import com.cczu.wuxin.mapper.CompetitionMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CrawlerService {

    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    // 扩展关键词列表：通用竞赛词 + 具体赛事名
    private static final List<String> KEYWORDS = Arrays.asList(
            // 通用竞赛词
            "竞赛", "比赛", "大赛", "竞技", "挑战赛",
            "报名", "参赛", "选拔", "评审", "答辩", "获奖", "喜报",
            // 具体赛事名
            "蓝桥杯", "数学建模", "ACM", "电子设计", "机械创新",
            "挑战杯", "互联网+", "智能车", "机器人", "创新大赛",
            "程序设计", "算法", "编程", "物理实验", "化学实验",
            "力学竞赛", "结构设计", "节能减排", "工程训练", "创业计划", "职业规划"
    );

    // 截止日期提取正则
    private static final Pattern[] DEADLINE_PATTERNS = {
            Pattern.compile("截止日期[：:]\\s*(\\d{4}[-年/.](\\d{1,2})[-月/.](\\d{1,2}))"),
            Pattern.compile("报名截止[：:]\\s*(\\d{4}[-年/.](\\d{1,2})[-月/.](\\d{1,2}))"),
            Pattern.compile("截止时间[：:]\\s*(\\d{4}[-年/.](\\d{1,2})[-月/.](\\d{1,2}))"),
            Pattern.compile("(\\d{4}[-年/.](\\d{1,2})[-月/.](\\d{1,2}))[前日]?\\s*(?:截止|前)")
    };

    // 组织单位提取正则
    private static final Pattern[] ORGANIZER_PATTERNS = {
            Pattern.compile("主办单位[：:]\\s*(.+?)[\\n，。;]"),
            Pattern.compile("承办单位[：:]\\s*(.+?)[\\n，。;]"),
            Pattern.compile("组织单位[：:]\\s*(.+?)[\\n，。;]")
    };
    private static final List<String> FALLBACK_ITEM_SELECTORS = Arrays.asList(
            "li.news",
            "li:has(a[href*=page])",
            "tr.listnews_listbottomline",
            "tr:has(table)"
    );
    private static final List<String> FALLBACK_TITLE_SELECTORS = Arrays.asList(
            "span.news_title a[href]",
            "a[href*=page]",
            "td a[href]",
            "a[href]"
    );
    private static final List<String> FALLBACK_DATE_SELECTORS = Arrays.asList(
            "span.news_meta",
            "td:last-child",
            "div",
            "span"
    );

    private final CompetitionMapper competitionMapper;
    private final EmailService emailService;
    private final CrawlerProperties crawlerProperties;

    public CrawlerService(CompetitionMapper competitionMapper, EmailService emailService,
                          CrawlerProperties crawlerProperties) {
        this.competitionMapper = competitionMapper;
        this.emailService = emailService;
        this.crawlerProperties = crawlerProperties;
    }

    /**
     * 主爬虫方法：遍历所有配置的源
     */
    public void crawl() {
        List<CrawlerProperties.Source> sources = crawlerProperties.getSources();
        if (sources == null || sources.isEmpty()) {
            log.warn("未配置任何爬虫源");
            return;
        }

        List<String> newTitles = new ArrayList<>();
        List<String> newUrls = new ArrayList<>();

        for (CrawlerProperties.Source source : sources) {
            log.info("开始爬取 [{}] - {}", source.getCollege(), source.getBaseUrl());
            try {
                crawlSource(source, newTitles, newUrls);
            } catch (Exception e) {
                log.error("爬取 [{}] 失败: {}", source.getCollege(), e.getMessage());
            }
        }

        if (!newTitles.isEmpty()) {
            log.info("本次共发现 {} 条新公告", newTitles.size());
            try {
                emailService.sendNewCompetitionNotice(newTitles, newUrls);
            } catch (Exception e) {
                log.error("发送邮件通知失败", e);
            }
        } else {
            log.info("本次未发现新公告");
        }
    }

    private void crawlSource(CrawlerProperties.Source source,
                              List<String> newTitles, List<String> newUrls) {
        String domain = extractDomain(source.getBaseUrl());

        for (int page = 1; page <= source.getPages(); page++) {
            try {
                String pageUrl = buildPageUrl(source.getBaseUrl(), page);
                log.info("  [{}] 第 {} 页: {}", source.getCollege(), page, pageUrl);

                Document doc = Jsoup.connect(pageUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(15000)
                        .get();

                Elements items = selectItems(doc, source.getSelector());
                log.info("  [{}] 第 {} 页找到 {} 条", source.getCollege(), page, items.size());

                for (Element item : items) {
                    try {
                        processItem(item, source, domain, newTitles, newUrls);
                    } catch (Exception e) {
                        log.warn("  处理条目失败: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("  [{}] 第 {} 页失败: {}", source.getCollege(), page, e.getMessage());
            }
        }
    }

    private void processItem(Element item, CrawlerProperties.Source source,
                              String domain, List<String> newTitles, List<String> newUrls) throws Exception {
        Element link = selectFirst(item, source.getTitleSelector(), FALLBACK_TITLE_SELECTORS);
        if (link == null) return;

        String title = link.attr("title");
        if (title.isEmpty()) {
            title = link.text().trim();
        }
        if (title.isEmpty()) return;

        // 关键词过滤
        boolean matched = KEYWORDS.stream().anyMatch(title::contains);
        if (!matched) return;

        String href = link.attr("abs:href");
        if (href == null || href.trim().isEmpty()) {
            href = link.attr("href");
        }
        if (href == null || href.trim().isEmpty()) return;
        href = href.trim();
        if (!href.startsWith("http")) {
            href = domain + (href.startsWith("/") ? "" : "/") + href;
        }

        // 去重
        if (competitionMapper.existsByUrl(href)) return;

        // 解析发布日期
        LocalDate publishDate = parseDate(item, source.getDateSelector());

        // 抓取详情页内容
        String content = fetchDetail(href);

        // 自动检测分类
        String category = detectCategory(title);

        // v1.2 新增：检测比赛级别、提取截止日期和组织单位
        String level = detectLevel(title);
        LocalDate deadline = extractDeadline(content);
        String organizer = extractOrganizer(content);

        Competition comp = new Competition(title, href, content, publishDate,
                source.getCollege(), category, level, deadline, organizer);
        competitionMapper.insert(comp);

        newTitles.add("[" + source.getCollege() + "] " + title);
        newUrls.add(href);
        log.info("  新增公告: [{}][{}][{}] {}", source.getCollege(), category,
                level != null ? level : "-", title);
    }

    /**
     * 根据标题关键词自动检测分类
     */
    static String detectCategory(String title) {
        if (title.contains("获奖") || title.contains("喜报") || title.contains("表彰")) {
            return "获奖喜报";
        }
        if (title.contains("报名") || title.contains("参赛") || title.contains("选拔") || title.contains("注册")) {
            return "报名通知";
        }
        if (title.contains("竞赛") || title.contains("比赛") || title.contains("大赛")
                || title.contains("竞技") || title.contains("挑战赛")) {
            return "竞赛公告";
        }
        return "其他通知";
    }

    /**
     * 根据标题关键词检测比赛级别
     */
    static String detectLevel(String title) {
        if (title.contains("全国") || title.contains("国赛") || title.contains("国家级")) {
            return "国赛";
        }
        if (title.contains("省级") || title.contains("省赛") || title.contains("江苏省")) {
            return "省赛";
        }
        if (title.contains("校级") || title.contains("校赛") || title.contains("校内") || title.contains("院内")) {
            return "校赛";
        }
        return null;
    }

    /**
     * 从详情页内容中正则提取截止日期
     */
    static LocalDate extractDeadline(String content) {
        if (content == null || content.isEmpty()) return null;
        for (Pattern p : DEADLINE_PATTERNS) {
            Matcher m = p.matcher(content);
            if (m.find()) {
                String dateStr = m.group(1)
                        .replace("年", "-").replace("月", "-").replace(".", "-").replace("/", "-");
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-M-d"));
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    /**
     * 从详情页内容中正则提取组织单位
     */
    static String extractOrganizer(String content) {
        if (content == null || content.isEmpty()) return null;
        for (Pattern p : ORGANIZER_PATTERNS) {
            Matcher m = p.matcher(content);
            if (m.find()) {
                String org = m.group(1).trim();
                if (!org.isEmpty() && org.length() <= 200) {
                    return org;
                }
            }
        }
        return null;
    }

    private String buildPageUrl(String baseUrl, int page) {
        if (page == 1) return baseUrl;
        if (baseUrl.contains("list.htm")) {
            return baseUrl.replace("list.htm", "list" + page + ".htm");
        }
        if (baseUrl.contains("list.psp")) {
            return baseUrl.replace("list.psp", "list" + page + ".psp");
        }
        return baseUrl;
    }

    private String extractDomain(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            return u.getProtocol() + "://" + u.getHost();
        } catch (Exception e) {
            return "https://www.cczu.edu.cn";
        }
    }

    private Elements selectItems(Document doc, String configuredSelector) {
        if (configuredSelector != null && !configuredSelector.trim().isEmpty()) {
            Elements configured = doc.select(configuredSelector);
            if (!configured.isEmpty()) {
                return configured;
            }
        }
        for (String selector : FALLBACK_ITEM_SELECTORS) {
            Elements items = doc.select(selector);
            if (!items.isEmpty()) {
                return items;
            }
        }
        return new Elements();
    }

    private Element selectFirst(Element item, String configuredSelector, List<String> fallbackSelectors) {
        if (configuredSelector != null && !configuredSelector.trim().isEmpty()) {
            Element el = item.selectFirst(configuredSelector);
            if (el != null) {
                return el;
            }
        }
        for (String selector : fallbackSelectors) {
            Element el = item.selectFirst(selector);
            if (el != null) {
                return el;
            }
        }
        return null;
    }

    private LocalDate parseDate(Element item, String configuredDateSelector) {
        if (configuredDateSelector != null && !configuredDateSelector.trim().isEmpty()) {
            LocalDate parsed = parseDateBySelector(item, configuredDateSelector);
            if (parsed != null) {
                return parsed;
            }
        }
        for (String selector : FALLBACK_DATE_SELECTORS) {
            LocalDate parsed = parseDateBySelector(item, selector);
            if (parsed != null) {
                return parsed;
            }
        }
        return tryParseDate(item.text());
    }

    private LocalDate parseDateBySelector(Element item, String selector) {
        Element dateEl = item.selectFirst(selector);
        if (dateEl == null) return null;
        String text = dateEl.text();
        if (text == null || text.trim().isEmpty()) return null;
        return tryParseDate(text.trim());
    }

    private LocalDate tryParseDate(String text) {
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyy.MM.dd")
        };
        String dateStr = text.replaceAll(".*?(\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}).*", "$1");
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(dateStr, fmt);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String fetchDetail(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();
            Element content = doc.selectFirst("div.wp_articlecontent");
            if (content == null) content = doc.selectFirst("div.read");
            if (content == null) content = doc.selectFirst("div.article-content");
            if (content == null) content = doc.selectFirst("div.content");
            if (content != null) {
                content.select("img").remove();
                return content.text();
            }
            return "";
        } catch (Exception e) {
            log.warn("  抓取详情页失败: {} - {}", url, e.getMessage());
            return "";
        }
    }
}
