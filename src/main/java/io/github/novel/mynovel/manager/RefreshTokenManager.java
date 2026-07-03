package io.github.novel.mynovel.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.github.novel.mynovel.core.constant.CacheConsts;

import java.util.concurrent.TimeUnit;

/**
 * Refresh Token Redis 管理器
 * <p>
 * 负责 Refresh Token 的存储、验证、轮换和撤销。
 * Redis 键格式：novel:refresh_token:{tokenId}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenManager {

    private final RedissonClient redissonClient;

    @Value("${jwt.refresh-token-expire:604800}")
    private Long refreshTokenExpireSeconds;

    private static final String REDIS_KEY_PREFIX = CacheConsts.REDIS_CACHE_PREFIX + "refresh_token:";

    /**
     * 保存 Refresh Token（登录/注册时调用）
     *
     * @param tokenId Token 唯一标识
     * @param userId  用户 ID
     */
    public void saveRefreshToken(String tokenId, Long userId) {
        RBucket<Long> bucket = redissonClient.getBucket(REDIS_KEY_PREFIX + tokenId);
        bucket.set(userId, refreshTokenExpireSeconds, TimeUnit.SECONDS);
        log.debug("RefreshToken saved: tokenId={}, userId={}", tokenId, userId);
    }

    /**
     * 验证 Refresh Token 在 Redis 中是否存在且有效
     *
     * @param tokenId Token 唯一标识
     * @return 用户 ID（有效）；null（不存在或已过期）
     */
    public Long validateRefreshToken(String tokenId) {
        RBucket<Long> bucket = redissonClient.getBucket(REDIS_KEY_PREFIX + tokenId);
        Long userId = bucket.get();
        if (userId == null) {
            log.warn("RefreshToken invalid or expired in Redis: tokenId={}", tokenId);
        }
        return userId;
    }

    /**
     * 删除 Refresh Token（退出登录时调用）
     *
     * @param tokenId Token 唯一标识
     */
    public void removeRefreshToken(String tokenId) {
        RBucket<Long> bucket = redissonClient.getBucket(REDIS_KEY_PREFIX + tokenId);
        bucket.delete();
        log.debug("RefreshToken removed: tokenId={}", tokenId);
    }

    /**
     * 轮换 Refresh Token：删除旧 token，保存新 token
     * <p>
     * 旧 token 立即失效，有效防止 Refresh Token 重放攻击。
     *
     * @param oldTokenId 旧 Token 唯一标识
     * @param newTokenId 新 Token 唯一标识
     * @param userId     用户 ID
     */
    public void rotateRefreshToken(String oldTokenId, String newTokenId, Long userId) {
        removeRefreshToken(oldTokenId);
        saveRefreshToken(newTokenId, userId);
        log.debug("RefreshToken rotated: old={}, new={}, userId={}", oldTokenId, newTokenId, userId);
    }
}
