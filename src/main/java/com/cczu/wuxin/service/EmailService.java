package com.cczu.wuxin.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    // 邮件发送器可选注入：未配置 spring.mail.host 时为 null
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.notify-to:}")
    private String notifyTo;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNewCompetitionNotice(List<String> titles, List<String> urls) {
        if (titles.isEmpty()) return;

        if (!mailEnabled) {
            log.info("邮件通知已关闭，跳过发送（本次发现 {} 条新公告）", titles.size());
            return;
        }

        if (mailSender == null) {
            log.warn("邮件发送器未配置（spring.mail.host 缺失），跳过发送");
            return;
        }

        if (fromEmail == null || fromEmail.isBlank() || fromEmail.contains("your_email")) {
            log.warn("邮箱未配置，跳过邮件发送");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(notifyTo.split(","));
            helper.setSubject("【cczu-无心】发现 " + titles.size() + " 条新竞赛公告");

            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            StringBuilder html = new StringBuilder();
            html.append("<div style=\"max-width:600px;margin:0 auto;font-family:'Segoe UI',Arial,sans-serif;\">");
            html.append("<div style=\"background:linear-gradient(135deg,#1a73e8,#0d47a1);padding:24px;border-radius:12px 12px 0 0;text-align:center;\">");
            html.append("<h1 style=\"color:#fff;margin:0;font-size:22px;\">cczu-无心</h1>");
            html.append("<p style=\"color:rgba(255,255,255,0.8);margin:4px 0 0;font-size:13px;\">常州大学竞赛信息聚合平台</p>");
            html.append("</div>");
            html.append("<div style=\"background:#fff;padding:24px;border:1px solid #e8eaed;border-top:none;\">");
            html.append("<p style=\"color:#333;font-size:15px;margin:0 0 16px;\">发现 <strong>").append(titles.size()).append("</strong> 条新竞赛公告：</p>");

            for (int i = 0; i < titles.size(); i++) {
                html.append("<div style=\"padding:12px 16px;margin-bottom:8px;background:#f8f9fa;border-radius:8px;border-left:3px solid #1a73e8;\">");
                html.append("<a href=\"").append(urls.get(i)).append("\" style=\"color:#1a73e8;text-decoration:none;font-size:14px;font-weight:600;\">")
                    .append(titles.get(i)).append("</a>");
                html.append("</div>");
            }

            html.append("</div>");
            html.append("<div style=\"background:#f8f9fa;padding:16px 24px;border-radius:0 0 12px 12px;border:1px solid #e8eaed;border-top:none;text-align:center;\">");
            html.append("<p style=\"color:#999;font-size:12px;margin:0;\">").append(time).append(" · cczu-无心 自动通知</p>");
            html.append("</div></div>");

            helper.setText(html.toString(), true);
            mailSender.send(message);
            log.info("邮件通知发送成功，共 {} 条新公告", titles.size());
        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", e.getMessage());
        }
    }
}
