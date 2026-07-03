package io.github.novel.mynovel.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * JWT 工具类 — 双 Token 方案（Access Token + Refresh Token）
 */
@ConditionalOnProperty("jwt.secret")
@Component
@Slf4j
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire:1800}")
    private Long accessTokenExpireSeconds;

    @Value("${jwt.refresh-token-expire:604800}")
    private Long refreshTokenExpireSeconds;

    private static final String HEADER_SYSTEM_KEY = "systemKeyHeader";
    private static final String CLAIM_TOKEN_ID = "tokenId";

    // ==================== 原有方法,保留向后兼容 ====================

    /**
     * 根据用户 ID 生成 JWT（无过期时间，保留用于兼容旧逻辑）
     */
    public String generateToken(Long uid, String systemKey) {
        return Jwts.builder()
            .header()
            .add(HEADER_SYSTEM_KEY, systemKey)
            .and()
            .subject(uid.toString())
            .signWith(getSecretKey())
            .compact();
    }

    /**
     * 解析 JWT 返回用户 ID（吞掉异常，保留用于兼容旧逻辑）
     */
    public Long parseToken(String token, String systemKey) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            if (Objects.equals(claimsJws.getHeader().get(HEADER_SYSTEM_KEY), systemKey)) {
                return Long.parseLong(claimsJws.getBody().getSubject());
            }
        } catch (JwtException e) {
            log.warn("JWT解析失败：{}", token);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // ==================== 双 Token 新增方法 ====================

    /**
     * 生成 Access Token（短有效期，用于业务接口认证）
     *
     * @param uid       用户 ID
     * @param systemKey 系统标识
     * @return Access Token JWT 字符串
     */
    public String generateAccessToken(Long uid, String systemKey) {
        Instant now = Instant.now();
        return Jwts.builder()
            .header()
            .add(HEADER_SYSTEM_KEY, systemKey)
            .and()
            .subject(uid.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(accessTokenExpireSeconds)))
            .signWith(getSecretKey())
            .compact();
    }

    /**
     * 生成 Refresh Token（长有效期，用于刷新 Access Token）
     * <p>
     * 同时生成唯一的 tokenId 存入 claims，用于 Redis 端管理生命周期。
     *
     * @param uid 用户 ID
     * @return Refresh Token JWT 字符串
     */
    public String generateRefreshToken(Long uid) {
        Instant now = Instant.now();
        String tokenId = UUID.randomUUID().toString().replace("-", "");
        return Jwts.builder()
            .subject(uid.toString())
            .claim(CLAIM_TOKEN_ID, tokenId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(refreshTokenExpireSeconds)))
            .signWith(getSecretKey())
            .compact();
    }

    /**
     * 解析 Access Token 并返回用户 ID。
     * <p>
     * <b>不会吞掉异常</b>——过期等异常由调用方捕获并区分处理。
     *
     * @param token     JWT
     * @param systemKey 系统标识
     * @return 用户 ID；若 systemKey 不匹配则返回 null
     * @throws ExpiredJwtException Access Token 已过期
     * @throws JwtException        其他 JWT 解析异常
     */
    public Long parseAccessToken(String token, String systemKey) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token);
        if (Objects.equals(claimsJws.getHeader().get(HEADER_SYSTEM_KEY), systemKey)) {
            return Long.parseLong(claimsJws.getBody().getSubject());
        }
        return null;
    }

    /**
     * 解析 Refresh Token 并提取用户 ID 与 tokenId。
     * <p>
     * 仅做 JWT 层面的解析与签名验证，不检查 Redis 端是否存在。
     *
     * @param token Refresh Token JWT 字符串
     * @return RefreshTokenData(userId, tokenId)
     * @throws ExpiredJwtException Refresh Token JWT 已过期
     * @throws JwtException        其他 JWT 解析异常
     */
    public RefreshTokenData parseRefreshToken(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token);
        return new RefreshTokenData(
                Long.parseLong(claimsJws.getBody().getSubject()),
                claimsJws.getBody().get(CLAIM_TOKEN_ID, String.class)
        );
    }

    /**
     * Refresh Token 解析结果
     */
    public record RefreshTokenData(Long userId, String tokenId) {}

    // ==================== 内部工具 ====================

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
