package io.github.novel.mynovel.manager;

import io.github.novel.mynovel.core.common.constant.ErrorCodeEnum;
import io.github.novel.mynovel.core.common.exception.BusinessException;
import io.github.novel.mynovel.dao.entity.UserInfo;
import io.github.novel.mynovel.dao.mapper.UserInfoMapper;
import io.github.novel.mynovel.dto.UserInfoDto;
import io.github.novel.mynovel.manager.cache.UserInfoCacheManager;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserVipManager {

    private final UserInfoMapper userInfoMapper;
    private final UserInfoCacheManager userInfoCacheManager;

    public boolean isVip(Long userId) {
        UserInfoDto user = userInfoCacheManager.getUser(userId);
        return user != null
                && user.getVipExpireTime() != null
                && user.getVipExpireTime().isAfter(LocalDateTime.now());
    }

    public LocalDateTime getVipExpireTime(Long userId) {
        if (userId == null) {
            return null;
        }
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return userInfo == null ? null : userInfo.getVipExpireTime();
    }

    @Transactional
    public LocalDateTime openOrRenewVip(Long userId, int durationDays) {
        int updated = userInfoMapper.renewVip(userId, durationDays);
        if (updated == 0) {
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }
        userInfoCacheManager.evictUser(userId);
        return getVipExpireTime(userId);
    }

    public void requireVip(Long userId) {
        if (!isVip(userId)) {
            throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        }
    }
}
