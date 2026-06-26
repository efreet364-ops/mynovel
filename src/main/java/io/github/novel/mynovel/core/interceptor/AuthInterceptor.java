package io.github.novel.mynovel.core.interceptor;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.novel.mynovel.core.auth.AuthStrategy;
import io.github.novel.mynovel.core.auth.UserHolder;
import io.github.novel.mynovel.core.common.exception.BusinessException;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.constant.ApiRouterConsts;
import io.github.novel.mynovel.core.constant.SystemConfigConsts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    // 注入所有认证策略
    private final Map<String, AuthStrategy> authStrategy;

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取jwt令牌
        String token = request.getHeader(SystemConfigConsts.HTTP_AUTH_HEADER_NAME);
        // 获取URI
        String uri = request.getRequestURI();
        // 根据URI选择认证策略
        String subUri = StrUtil.subSuf(uri, ApiRouterConsts.API_URL_PREFIX.length());
        String systemName = subUri.substring(0, subUri.indexOf("/"));
        String authStrategyName = systemName + "AuthStrategy";

        // 根据认证策略认证
        try {
            authStrategy.get(authStrategyName).auth(token, uri);
            return HandlerInterceptor.super.preHandle(request, response, handler);
        } catch (BusinessException exception) {
            // 认证失败
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    objectMapper.writeValueAsString(RestResp.fail(exception.getErrorCodeEnum())));
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
