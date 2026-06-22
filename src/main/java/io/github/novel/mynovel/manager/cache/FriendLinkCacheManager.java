package io.github.novel.mynovel.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.novel.mynovel.core.constant.CacheConsts;
import io.github.novel.mynovel.core.constant.DatabaseConsts;
import io.github.novel.mynovel.dao.entity.HomeFriendLink;
import io.github.novel.mynovel.dao.mapper.HomeFriendLinkMapper;
import io.github.novel.mynovel.dto.resp.HomeFriendLinkRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 友情链接 缓存管理类
 *
 */
@Component
@RequiredArgsConstructor
public class FriendLinkCacheManager {

    private final HomeFriendLinkMapper homeFriendLinkMapper;

    @Cacheable(cacheManager = CacheConsts.REDIS_CACHE_MANAGER
            , value = CacheConsts.HOME_FRIEND_LINK_CACHE_NAME)
    public List<HomeFriendLinkRespDto> listFriendLinks() {
        QueryWrapper<HomeFriendLink> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc(DatabaseConsts.CommonColumnEnum.SORT.getName());
        List<HomeFriendLink> homeFriendLinks = homeFriendLinkMapper.selectList(queryWrapper);

        return homeFriendLinks.stream()
                .map(link -> {
                    HomeFriendLinkRespDto dto = new HomeFriendLinkRespDto();
                    dto.setLinkName(link.getLinkName());
                    dto.setLinkUrl(link.getLinkUrl());
                    return dto;
                })
                .toList();

    }
}
