package io.github.novel.mynovel.manager.cache;


import cn.hutool.core.bean.BeanUtil;
import io.github.novel.mynovel.core.constant.CacheConsts;
import io.github.novel.mynovel.dao.mapper.NewsInfoMapper;
import io.github.novel.mynovel.dto.resp.NewsInfoRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 新闻资讯 缓存管理类
 *
 */
@Component
@RequiredArgsConstructor
public class NewsCacheManager {

    private final NewsInfoMapper newsInfoMapper;

    /*
    * 最新新闻资讯 查询并放入缓存
    */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER
                , value = CacheConsts.LATEST_NEWS_CACHE_NAME)
    public List<NewsInfoRespDto> listLatestNews() {

        return newsInfoMapper.selectNewsList().stream()
                .map(newsinfo -> {
                    NewsInfoRespDto dto = new NewsInfoRespDto();
                    BeanUtil.copyProperties(newsinfo, dto);
                    return dto;
                }).toList();
    }

}
