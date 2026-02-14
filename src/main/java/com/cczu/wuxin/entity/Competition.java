package com.cczu.wuxin.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Competition {

    private Long id;
    private String title;
    private String url;
    private String content;
    private LocalDate publishDate;
    private LocalDateTime crawlTime;
    private String college;
    private String category;

    public Competition() {}

    public Competition(String title, String url, String content, LocalDate publishDate) {
        this.title = title;
        this.url = url;
        this.content = content;
        this.publishDate = publishDate;
    }

    public Competition(String title, String url, String content, LocalDate publishDate, String college, String category) {
        this.title = title;
        this.url = url;
        this.content = content;
        this.publishDate = publishDate;
        this.college = college;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }

    public LocalDateTime getCrawlTime() { return crawlTime; }
    public void setCrawlTime(LocalDateTime crawlTime) { this.crawlTime = crawlTime; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
