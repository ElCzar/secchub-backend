package co.edu.puj.secchub_backend.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Cache configuration for the application.
 * Enables caching with Caffeine as the cache provider.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Configures Caffeine cache manager for high-performance caching.
     * Optimized for parametric/lookup values that are frequently accessed but rarely change.
     * Async mode enabled for reactive method support.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)                    // Max 1000 entries per cache
            .expireAfterWrite(2, TimeUnit.HOURS)  // Cache for 2 hours
            .recordStats());                      // Enable cache statistics
        cacheManager.setAsyncCacheMode(true);     // Enable async mode for reactive methods
        return cacheManager;
    }
}
