package io.github.novel.mynovel.controller.front;

import io.github.novel.mynovel.core.auth.UserHolder;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.constant.ApiRouterConsts;
import io.github.novel.mynovel.core.constant.SystemConfigConsts;
import io.github.novel.mynovel.dto.req.UserCommentReqDto;
import io.github.novel.mynovel.dto.req.UserLoginReqDto;
import io.github.novel.mynovel.dto.req.UserRegisterReqDto;
import io.github.novel.mynovel.dto.resp.UserInfoRespDto;
import io.github.novel.mynovel.dto.req.UserInfoUptReqDto;
import io.github.novel.mynovel.dto.resp.UserLoginRespDto;
import io.github.novel.mynovel.dto.resp.UserRegisterRespDto;
import io.github.novel.mynovel.service.BookService;
import io.github.novel.mynovel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
@SecurityRequirement(name = SystemConfigConsts.HTTP_AUTH_HEADER_NAME)
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_USER_URL_PREFIX)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final BookService bookService;

    /**
     * 用户注册接口
     */
    @Operation(summary = "用户注册接口")
    @PostMapping("register")
    public RestResp<UserRegisterRespDto> register(@Valid @RequestBody UserRegisterReqDto dto) {
        return userService.register(dto);
    }

    /**
     * 用户登录接口
     */
    @Operation(summary = "用户登录接口")
    @PostMapping("login")
    public RestResp<UserLoginRespDto> login(@Valid @RequestBody UserLoginReqDto dto) {
        return userService.login(dto);
    }

    /**
     * 用户信息查询接口
     */
    @Operation(summary = "用户信息查询接口")
    @GetMapping
    public RestResp<UserInfoRespDto> getUserInfo() {
        return userService.getUserInfo(UserHolder.getUserId());
    }

    /**
     * 用户信息修改接口
     */
    @Operation(summary = "用户信息修改接口")
    @PutMapping
    public RestResp<Void> updateUserInfo(@Valid @RequestBody UserInfoUptReqDto dto) {
        dto.setUserId(UserHolder.getUserId());
        return userService.updateUserInfo(dto);
    }

    /**
     * 发表评论接口
     */
    @Operation(summary = "发表评论接口")
    @PostMapping("comment")
    public RestResp<Void> comment(@Valid @RequestBody UserCommentReqDto dto) {
        dto.setUserId(UserHolder.getUserId());
        return bookService.saveComment(dto);
    }
}
