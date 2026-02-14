package com.cczu.wuxin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.cczu.wuxin.mapper")
public class CczuWuxinApplication {

    public static void main(String[] args) {
        SpringApplication.run(CczuWuxinApplication.class, args);
    }
}
