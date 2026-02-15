package com.cczu.wuxin;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.cczu.wuxin.mapper")
public class CczuWuxinApplication {

    private static final Logger log = LoggerFactory.getLogger(CczuWuxinApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CczuWuxinApplication.class, args);
    }

    /**
     * 启动完成后自动打开浏览器访问首页
     */
    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:8888";
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                log.info("已自动打开浏览器：{}", url);
            } else {
                // 备用方案：Windows 命令行打开
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
                log.info("已通过命令行打开浏览器：{}", url);
            }
        } catch (Exception e) {
            log.warn("无法自动打开浏览器，请手动访问：{}", url);
        }
    }
}
