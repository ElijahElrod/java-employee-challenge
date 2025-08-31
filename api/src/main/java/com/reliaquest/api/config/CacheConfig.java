package com.reliaquest.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the application using Caffeine as the provider.
 * <p>
 * This class enables Spring's caching abstraction via {@link EnableCaching} and
 * registers beans to customize the Caffeine cache settings and the {@link CacheManager}.
 * </p>
 *
 * <ul>
 *   <li>{@link #caffeineConfig()} – Configures a {@link Caffeine} builder with:
 *       <ul>
 *         <li>Entries expiring 5 minutes after write</li>
 *         <li>Initial capacity of 100 entries</li>
 *       </ul>
 *   </li>
 *   <li>{@link #cacheManager(Caffeine)} – Provides a {@link CacheManager}
 *       backed by {@link CaffeineCacheManager}, using the custom {@link Caffeine} configuration.</li>
 * </ul>
 *
 * <p>
 * This setup allows application components to use Spring's caching annotations
 * (e.g., {@code @Cacheable}, {@code @CacheEvict}, {@code @CachePut}) with a
 * high-performance in-memory cache implementation.
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final int EXPIRY_M = 5;
    private static final int INITIAL_CAPACITY = 100;

    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(EXPIRY_M, TimeUnit.MINUTES)
                .initialCapacity(INITIAL_CAPACITY);
    }

    @Bean
    public CacheManager cacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}
