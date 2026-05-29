package com.novelreading.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置跨域 (CORS)，支持 Vue.js 前端跨域访问 RESTful API
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 前端开发环境（Vite/React）可能会自动换端口，这里放行常见本机端口
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173", "http://localhost:5174", "http://localhost:5175",
                        "http://127.0.0.1:3000",
                        "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
