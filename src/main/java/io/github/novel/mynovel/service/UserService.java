package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dao.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.novel.mynovel.dto.req.UserLoginReqDto;
import io.github.novel.mynovel.dto.req.UserRegisterReqDto;
import io.github.novel.mynovel.dto.resp.UserInfoRespDto;
import io.github.novel.mynovel.dto.req.UserInfoUptReqDto;
import io.github.novel.mynovel.dto.resp.UserLoginRespDto;
import io.github.novel.mynovel.dto.resp.UserRegisterRespDto;
import jakarta.validation.Valid;

/**
 * <p>
 * 系统用户 服务类
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 */
public interface UserService extends IService<SysUser> {

    /**
     * 用户注册
     *
     * @param dto 注册参数
     * @return JWT + 昵称
     */
    RestResp<UserRegisterRespDto> register(UserRegisterReqDto dto);

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return JWT + RefreshToken + 昵称
     */
    RestResp<UserLoginRespDto> login(UserLoginReqDto dto);

    /**
     * 用户信息查询接口
     */
    RestResp<UserInfoRespDto> getUserInfo(Long userId);

    /**
     * 用户信息修改
     *
     * @param dto 用户信息
     * @return void
     */
    RestResp<Void> updateUserInfo(@Valid UserInfoUptReqDto dto);

    /**
     * 刷新 Token（使用 Refresh Token 换取新的 Access Token + Refresh Token）
     *
     * @param refreshToken 旧的 Refresh Token
     * @return 新的 Token 对
     */
    RestResp<UserLoginRespDto> refreshToken(String refreshToken);

    /**
     * 退出登录（撤销 Refresh Token）
     *
     * @param refreshToken 当前的 Refresh Token
     * @return void
     */
    RestResp<Void> logout(String refreshToken);
}
