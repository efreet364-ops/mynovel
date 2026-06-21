package io.github.novel.mynovel.controller.front;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.constant.ApiRouterConsts;
import io.github.novel.mynovel.core.constant.SystemConfigConsts;
import io.github.novel.mynovel.dto.req.UserRegisterReqDto;
import io.github.novel.mynovel.dto.resp.UserRegisterRespDto;
import io.github.novel.mynovel.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 系统用户 控制器
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 *
 */
@Tag(name = "UserController", description = "前台门户-会员模块")
//@SecurityRequirement(name = SystemConfigConsts.HTTP_AUTH_HEADER_NAME)
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_USER_URL_PREFIX)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("register")
    public RestResp<UserRegisterRespDto> register(@Valid @RequestBody UserRegisterReqDto dto) {
        return userService.register(dto);
    }
}
