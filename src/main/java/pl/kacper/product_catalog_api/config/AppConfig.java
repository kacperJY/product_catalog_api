package pl.kacper.product_catalog_api.config;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class AppConfig {

    @Value("${cache.max-size}")
    private Long maxCacheSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineSpec caffeineSpec = CaffeineSpec.parse("maximumSize=%s".formatted(maxCacheSize));
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeineSpec(caffeineSpec);
        return caffeineCacheManager;
    }
}
