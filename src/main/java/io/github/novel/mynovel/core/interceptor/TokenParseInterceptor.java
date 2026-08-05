package io.github.novel.mynovel.core.interceptor;

import io.github.novel.mynovel.core.auth.UserHolder;
import io.github.novel.mynovel.core.constant.SystemConfigConsts;
import io.github.novel.mynovel.core.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TokenParseInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(SystemConfigConsts.HTTP_AUTH_HEADER_NAME);
        if (StringUtils.hasText(token)) {
            try {
                Long userId = jwtUtils.parseAccessToken(token, SystemConfigConsts.NOVEL_FRONT_KEY);
                if (userId != null) {
                    UserHolder.setUserId(userId);
                }
            } catch (Exception ignored) {
                UserHolder.clear();
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }


}
