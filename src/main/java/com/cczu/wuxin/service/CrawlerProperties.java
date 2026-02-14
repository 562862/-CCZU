package com.cczu.wuxin.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 多源爬虫配置属性，对应 application.yml 中 app.crawler
 */
@Component
@ConfigurationProperties(prefix = "app.crawler")
public class CrawlerProperties {

    private List<Source> sources;

    public List<Source> getSources() { return sources; }
    public void setSources(List<Source> sources) { this.sources = sources; }

    public static class Source {
        private String name;
        private String college;
        private String baseUrl;
        private int pages = 3;
        private String selector;
        private String titleSelector;
        private String dateSelector;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public int getPages() { return pages; }
        public void setPages(int pages) { this.pages = pages; }

        public String getSelector() { return selector; }
        public void setSelector(String selector) { this.selector = selector; }

        public String getTitleSelector() { return titleSelector; }
        public void setTitleSelector(String titleSelector) { this.titleSelector = titleSelector; }

        public String getDateSelector() { return dateSelector; }
        public void setDateSelector(String dateSelector) { this.dateSelector = dateSelector; }
    }
}
