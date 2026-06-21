package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dao.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.novel.mynovel.dto.req.UserLoginReqDto;
import io.github.novel.mynovel.dto.req.UserRegisterReqDto;
import io.github.novel.mynovel.dto.resp.UserLoginRespDto;
import io.github.novel.mynovel.dto.resp.UserRegisterRespDto;

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
     * @return JWT
     */
    RestResp<UserRegisterRespDto> register(UserRegisterReqDto dto);

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return JWT + 昵称
     */
    RestResp<UserLoginRespDto> login(UserLoginReqDto dto);



}
