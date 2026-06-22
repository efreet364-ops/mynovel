package io.github.novel.mynovel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.constant.DatabaseConsts;
import io.github.novel.mynovel.dao.entity.NewsContent;
import io.github.novel.mynovel.dao.entity.NewsInfo;
import io.github.novel.mynovel.dao.mapper.NewsContentMapper;
import io.github.novel.mynovel.dao.mapper.NewsInfoMapper;
import io.github.novel.mynovel.dto.resp.NewsInfoRespDto;
import io.github.novel.mynovel.manager.cache.NewsCacheManager;
import io.github.novel.mynovel.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsCacheManager newsCacheManager;

    private final NewsInfoMapper newsInfoMapper;

    private final NewsContentMapper newsContentMapper;

    @Override
    public RestResp<List<NewsInfoRespDto>> listLatestNews() {
        return RestResp.ok(newsCacheManager.listLatestNews());
    }

    @Override
    public RestResp<NewsInfoRespDto> getNews(Long id) {
        NewsInfo newsInfo = newsInfoMapper.selectById(id);
        QueryWrapper<NewsContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.NewsContentTable.COLUMN_NEWS_ID, id)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        NewsContent newsContent = newsContentMapper.selectOne(queryWrapper);
        return RestResp.ok(NewsInfoRespDto.builder()
                .id(newsInfo.getId())
                .title(newsInfo.getTitle())
                .content(newsContent.getContent())
                .sourceName(newsInfo.getSourceName())
                .categoryName(newsInfo.getSourceName())
                .updateTime(newsInfo.getUpdateTime())
                .build()
        );
    }
}
