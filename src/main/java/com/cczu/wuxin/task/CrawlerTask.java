package com.cczu.wuxin.task;

import com.cczu.wuxin.service.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CrawlerTask {

    private static final Logger log = LoggerFactory.getLogger(CrawlerTask.class);

    private final CrawlerService crawlerService;

    public CrawlerTask(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    /**
     * 启动时立即执行一次
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("应用启动，执行首次爬取...");
        crawlerService.crawl();
    }

    /**
     * 每 15 分钟执行一次
     */
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void scheduledCrawl() {
        log.info("定时任务触发，开始爬取...");
        crawlerService.crawl();
    }
}
