package com.cczu.wuxin.service;

import com.cczu.wuxin.entity.Competition;
import com.cczu.wuxin.mapper.CompetitionMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CrawlerService {

    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    // 扩展关键词列表
    private static final List<String> KEYWORDS = Arrays.asList(
            "竞赛", "比赛", "大赛", "竞技", "挑战赛",
            "报名", "参赛", "选拔", "评审", "答辩", "获奖", "喜报"
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
     * 主爬虫方法：遍历所有配置的学院源
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

                // 使用配置的选择器，默认 li.news
                String selector = source.getSelector() != null ? source.getSelector() : "li.news";
                Elements items = doc.select(selector);
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
        // 使用配置的标题选择器，默认 span.news_title a[href]
        String titleSel = source.getTitleSelector() != null ? source.getTitleSelector() : "span.news_title a[href]";
        Element link = item.selectFirst(titleSel);
        if (link == null) return;

        String title = link.attr("title");
        if (title.isEmpty()) {
            title = link.text().trim();
        }
        if (title.isEmpty()) return;

        // 关键词过滤
        boolean matched = KEYWORDS.stream().anyMatch(title::contains);
        if (!matched) return;

        String href = link.attr("href");
        if (!href.startsWith("http")) {
            href = domain + (href.startsWith("/") ? "" : "/") + href;
        }

        // 去重
        if (competitionMapper.existsByUrl(href)) return;

        // 解析发布日期
        String dateSel = source.getDateSelector() != null ? source.getDateSelector() : "span.news_meta";
        LocalDate publishDate = parseDate(item, dateSel);

        // 抓取详情页内容
        String content = fetchDetail(href);

        // 自动检测分类
        String category = detectCategory(title);

        Competition comp = new Competition(title, href, content, publishDate, source.getCollege(), category);
        competitionMapper.insert(comp);

        newTitles.add("[" + source.getCollege() + "] " + title);
        newUrls.add(href);
        log.info("  新增公告: [{}][{}] {}", source.getCollege(), category, title);
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

    private String buildPageUrl(String baseUrl, int page) {
        if (page == 1) return baseUrl;
        return baseUrl.replace("list.htm", "list" + page + ".htm");
    }

    private String extractDomain(String url) {
        // 从 baseUrl 提取域名，如 https://jsjx.cczu.edu.cn/21177/list.htm -> https://jsjx.cczu.edu.cn
        try {
            java.net.URL u = new java.net.URL(url);
            return u.getProtocol() + "://" + u.getHost();
        } catch (Exception e) {
            return "https://www.cczu.edu.cn";
        }
    }

    private LocalDate parseDate(Element item, String dateSelector) {
        Element dateEl = item.selectFirst(dateSelector);
        if (dateEl != null) {
            return tryParseDate(dateEl.text().trim());
        }
        return null;
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
