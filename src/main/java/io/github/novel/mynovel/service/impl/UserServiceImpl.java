package io.github.novel.mynovel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.novel.mynovel.core.common.constant.ErrorCodeEnum;
import io.github.novel.mynovel.core.common.exception.BusinessException;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.constant.DatabaseConsts;
import io.github.novel.mynovel.core.constant.SystemConfigConsts;
import io.github.novel.mynovel.core.util.JwtUtils;
import io.github.novel.mynovel.core.util.JwtUtils.RefreshTokenData;
import io.github.novel.mynovel.dao.entity.SysUser;
import io.github.novel.mynovel.dao.entity.UserInfo;
import io.github.novel.mynovel.dao.mapper.SysUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.novel.mynovel.dao.mapper.UserInfoMapper;
import io.github.novel.mynovel.dto.req.UserLoginReqDto;
import io.github.novel.mynovel.dto.req.UserRegisterReqDto;
import io.github.novel.mynovel.dto.resp.UserInfoRespDto;
import io.github.novel.mynovel.dto.req.UserInfoUptReqDto;
import io.github.novel.mynovel.dto.resp.UserLoginRespDto;
import io.github.novel.mynovel.dto.resp.UserRegisterRespDto;
import io.github.novel.mynovel.manager.RefreshTokenManager;
import io.github.novel.mynovel.manager.VerifyCodeManager;
import io.github.novel.mynovel.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 系统用户 服务实现类
 * </p>
 *
 * @author efreet233
 * @since 2026/06/19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final VerifyCodeManager verifyCodeManager;

    private final UserInfoMapper userInfoMapper;

    private final JwtUtils jwtUtils;

    private final RefreshTokenManager refreshTokenManager;

    @Override
    public RestResp<UserRegisterRespDto> register(UserRegisterReqDto dto) {
        // 校验图形验证码是否正确
        if (!verifyCodeManager.imgVerifyCodeOk(dto.getSessionId(), dto.getVelCode())) {
            throw new BusinessException(ErrorCodeEnum.USER_VERIFY_CODE_ERROR);
        }

        // 校验手机号是否已注册（手机号代替username）
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserInfoTable.COLUMN_USERNAME, dto.getUsername())
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        if (userInfoMapper.selectCount(queryWrapper) > 0) {
            // 手机号已注册
            throw new BusinessException(ErrorCodeEnum.USER_NAME_EXIST);
        }

        // 注册成功，保存用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(
                DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        userInfo.setUsername(dto.getUsername());
        userInfo.setNickName(dto.getUsername());
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfo.setSalt("0");
        userInfoMapper.insert(userInfo);

        // 删除验证码
        verifyCodeManager.removeImgVerifyCode(dto.getSessionId());

        // 生成双 Token
        Long uid = userInfo.getId();
        String accessToken = jwtUtils.generateAccessToken(uid, SystemConfigConsts.NOVEL_FRONT_KEY);
        String refreshToken = jwtUtils.generateRefreshToken(uid);
        RefreshTokenData refreshTokenData = jwtUtils.parseRefreshToken(refreshToken);
        refreshTokenManager.saveRefreshToken(refreshTokenData.tokenId(), uid);

        return RestResp.ok(
                UserRegisterRespDto.builder()
                        .token(accessToken)
                        .refreshToken(refreshToken)
                        .uid(uid)
                        .build()
        );

    }

    @Override
    public RestResp<UserLoginRespDto> login(UserLoginReqDto dto) {
        // 查询用户信息
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserInfoTable.COLUMN_USERNAME, dto.getUsername())
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);

        if (Objects.isNull(userInfo)) {
            // 用户不存在
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }

        // 校验密码
        if (!Objects.equals(userInfo.getPassword()
                , DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)))) {
            // 密码错误
            throw new BusinessException(ErrorCodeEnum.USER_PASSWORD_ERROR);
        }

        // 登录成功，生成双 Token
        Long uid = userInfo.getId();
        String accessToken = jwtUtils.generateAccessToken(uid, SystemConfigConsts.NOVEL_FRONT_KEY);
        String refreshToken = jwtUtils.generateRefreshToken(uid);
        RefreshTokenData refreshTokenData = jwtUtils.parseRefreshToken(refreshToken);
        refreshTokenManager.saveRefreshToken(refreshTokenData.tokenId(), uid);

        return RestResp.ok(
                UserLoginRespDto.builder()
                        .uid(uid)
                        .nickName(userInfo.getNickName())
                        .token(accessToken)
                        .refreshToken(refreshToken)
                        .build()
        );
    }

    @Override
    public RestResp<UserLoginRespDto> refreshToken(String refreshToken) {
        // 解析 Refresh Token（JWT 层验证）
        RefreshTokenData refreshTokenData;
        try {
            refreshTokenData = jwtUtils.parseRefreshToken(refreshToken);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new BusinessException(ErrorCodeEnum.REFRESH_TOKEN_INVALID);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.REFRESH_TOKEN_INVALID);
        }

        // Redis 层验证
        Long userId = refreshTokenManager.validateRefreshToken(refreshTokenData.tokenId());
        if (userId == null) {
            throw new BusinessException(ErrorCodeEnum.REFRESH_TOKEN_INVALID);
        }

        // 生成新 Token 对
        String newAccessToken = jwtUtils.generateAccessToken(userId, SystemConfigConsts.NOVEL_FRONT_KEY);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId);
        RefreshTokenData newRefreshTokenData = jwtUtils.parseRefreshToken(newRefreshToken);

        // 轮换：删除旧 Refresh Token，保存新 Refresh Token（防止重放攻击）
        refreshTokenManager.rotateRefreshToken(
                refreshTokenData.tokenId(), newRefreshTokenData.tokenId(), userId);

        // 获取用户昵称
        UserInfo userInfo = userInfoMapper.selectById(userId);

        return RestResp.ok(
                UserLoginRespDto.builder()
                        .uid(userId)
                        .nickName(userInfo != null ? userInfo.getNickName() : null)
                        .token(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build()
        );
    }

    @Override
    public RestResp<Void> logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return RestResp.ok();
        }

        // 解析 Refresh Token 获取 tokenId
        RefreshTokenData refreshTokenData;
        try {
            refreshTokenData = jwtUtils.parseRefreshToken(refreshToken);
        } catch (Exception e) {
            // Refresh Token 无效，不报错，直接返回成功
            return RestResp.ok();
        }

        // 从 Redis 删除 Refresh Token
        refreshTokenManager.removeRefreshToken(refreshTokenData.tokenId());
        return RestResp.ok();
    }

    @Override
    public RestResp<UserInfoRespDto> getUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return RestResp.ok(UserInfoRespDto.builder()
                .nickName(userInfo.getNickName())
                .userSex(userInfo.getUserSex())
                .userPhoto(userInfo.getUserPhoto())
                .build());
    }

    @Override
    public RestResp<Void> updateUserInfo(UserInfoUptReqDto dto) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(dto.getUserId());
        userInfo.setNickName(dto.getNickName());
        userInfo.setUserPhoto(dto.getUserPhoto());
        userInfo.setUserSex(dto.getUserSex());
        userInfoMapper.updateById(userInfo);
        return RestResp.ok();
    }
}
