package io.github.novel.mynovel.service.impl;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.resp.HomeBookRespDto;
import io.github.novel.mynovel.manager.HomeBookCacheManager;
import io.github.novel.mynovel.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页模块 服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/13
 */
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomeBookCacheManager homeBookCacheManager;

    @Override
    public RestResp<List<HomeBookRespDto>> listHomeBooks() {
        return RestResp.ok(homeBookCacheManager.listHomeBooks());
    }
}

