package io.github.novel.mynovel.service.impl;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dao.mapper.BookInfoMapper;
import io.github.novel.mynovel.dto.resp.BookCategoryRespDto;
import io.github.novel.mynovel.dto.resp.BookInfoRespDto;
import io.github.novel.mynovel.manager.cache.BookCategoryCacheManager;
import io.github.novel.mynovel.manager.cache.BookInfoCacheManager;
import io.github.novel.mynovel.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookCategoryCacheManager bookCategoryCacheManager;

    private final BookInfoCacheManager bookInfoCacheManager;

    @Override
    public RestResp<List<BookCategoryRespDto>> listCategory(Integer workDirection) {
        return RestResp.ok(bookCategoryCacheManager.listCategory(workDirection));
    }

    @Override
    public RestResp<BookInfoRespDto> getBookById(Long bookId) {
        return RestResp.ok(bookInfoCacheManager.getBookInfo(bookId));
    }
}
