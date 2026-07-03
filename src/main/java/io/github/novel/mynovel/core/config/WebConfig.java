package io.github.novel.mynovel.core.config;

import io.github.novel.mynovel.core.constant.ApiRouterConsts;
import io.github.novel.mynovel.core.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 拦截会员中心相关请求接口
                .addPathPatterns(ApiRouterConsts.API_FRONT_USER_URL_PREFIX + "/**",
                        // 拦截作家后台相关请求接口
                        ApiRouterConsts.API_AUTHOR_URL_PREFIX + "/**",
                        // 拦截平台后台相关请求接口
                        ApiRouterConsts.API_ADMIN_URL_PREFIX + "/**")
                // 放行登录注册相关请求接口
                .excludePathPatterns(ApiRouterConsts.API_FRONT_USER_URL_PREFIX + "/register",
                        ApiRouterConsts.API_FRONT_USER_URL_PREFIX + "/login",
                        ApiRouterConsts.API_FRONT_USER_URL_PREFIX + "/token/refresh",
                        ApiRouterConsts.API_FRONT_USER_URL_PREFIX + "/logout",
                        ApiRouterConsts.API_ADMIN_URL_PREFIX + "/login");
    }
}
