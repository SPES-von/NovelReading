package com.novelreading.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 简易内存缓存配置
 * 当 Redis 不可用时作为 fallback，保障 @Cacheable 仍可工作
 */
@Configuration
public class SimpleCacheConfig {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("hotNovels", "featuredNovel", "completedNovels", "classicCompletedList", "publisherNovels", "publishers");
    }
}
