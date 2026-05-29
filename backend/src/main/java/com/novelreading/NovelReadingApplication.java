package com.novelreading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 轻小说阅读系统 - 主启动类
 * 技术栈：Spring Boot + MySQL + Redis + JWT + 百度语音合成
 */
@SpringBootApplication
@EnableCaching
public class NovelReadingApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelReadingApplication.class, args);
    }
}
