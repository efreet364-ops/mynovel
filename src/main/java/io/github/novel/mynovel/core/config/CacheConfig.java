package io.github.novel.mynovel.core.config;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.novel.mynovel.core.constant.CacheConsts;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class CacheConfig {

    /**
     * Caffeine 缓存管理器
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = new ArrayList<>(CacheConsts.CacheEnum.values().length);
        for (CacheConsts.CacheEnum c : CacheConsts.CacheEnum.values()) {
            if (c.isLocal()) {
                Caffeine<Object, Object> caffeine = Caffeine.newBuilder().recordStats().maximumSize(c.getMaxSize());
                if (c.getTtl() > 0) {
                    caffeine.expireAfterWrite(Duration.ofSeconds(c.getTtl()));
                }
                caches.add(new CaffeineCache(c.getName(), caffeine.build()));
            }
        }

        cacheManager.setCaches(caches);
        return cacheManager;
    }

    /**
     * Redis 缓存管理器
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();

        // 解决 LocalDateTime / LocalDate / LocalTime
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Redis 缓存需要可逆的时间格式，不能使用 DTO 上仅用于接口展示的 @JsonFormat。
        objectMapper.setAnnotationIntrospector(AnnotationIntrospector.nopInstance());
        // 解决 DTO、List<DTO> 反序列化成 LinkedHashMap
        objectMapper.activateDefaultTypingAsProperty(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                "@class"
        );

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisSerializationContext.SerializationPair<Object> valueSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(serializer);

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .prefixCacheNameWith(CacheConsts.REDIS_CACHE_PREFIX)
                .serializeValuesWith(valueSerializer);

        Map<String, RedisCacheConfiguration> cacheMap =
                new LinkedHashMap<>(CacheConsts.CacheEnum.values().length);
        for (CacheConsts.CacheEnum c : CacheConsts.CacheEnum.values()) {
            if (c.isRemote()) {
                RedisCacheConfiguration cacheConfig = defaultCacheConfig;
                if (c.getTtl() > 0) {
                    cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(c.getTtl()));
                }

                cacheMap.put(c.getName(), cacheConfig);
            }
        }

        RedisCacheWriter redisCacheWriter =
                RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        RedisCacheManager redisCacheManager =
                new RedisCacheManager(redisCacheWriter, defaultCacheConfig, cacheMap);
        redisCacheManager.setTransactionAware(true);
        redisCacheManager.initializeCaches();
        return redisCacheManager;
    }
}
