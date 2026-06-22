package io.github.novel.mynovel.manager.cache;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.novel.mynovel.core.constant.CacheConsts;
import io.github.novel.mynovel.core.constant.DatabaseConsts;
import io.github.novel.mynovel.dao.entity.BookChapter;
import io.github.novel.mynovel.dao.entity.BookInfo;
import io.github.novel.mynovel.dao.mapper.BookChapterMapper;
import io.github.novel.mynovel.dao.mapper.BookInfoMapper;
import io.github.novel.mynovel.dto.resp.BookInfoRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookInfoCacheManager {

    private final BookInfoMapper bookInfoMapper;

    private final BookChapterMapper bookChapterMapper;

    /**
     * 从缓存中查询小说信息（先判断缓存中是否已存在，存在则直接从缓存中取，否则执行方法体中的逻辑后缓存结果）
     */
    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_INFO_CACHE_NAME)
    public BookInfoRespDto getBookInfo(Long id) {
        return cachePutBookInfo(id);
    }

    /**
     * 缓存小说信息（不管缓存中是否存在都执行方法体中的逻辑，然后缓存起来）
     */
    @CachePut(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_INFO_CACHE_NAME)
    public BookInfoRespDto cachePutBookInfo(Long id) {
        // 查询基础信息
        BookInfo bookInfo = bookInfoMapper.selectById(id);
        // 查询首章ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, id)
                .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookChapter firstBookChapter = bookChapterMapper.selectOne(queryWrapper);
        // 组装响应对象
        BookInfoRespDto bookInfoRespDto = new BookInfoRespDto();
        BeanUtil.copyProperties(bookInfo, bookInfoRespDto);
        bookInfoRespDto.setFirstChapterId(firstBookChapter.getId());
        return bookInfoRespDto;
    }

    @CacheEvict(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.BOOK_INFO_CACHE_NAME)
    public void evictBookInfoCache(Long bookId) {
        // 调用此方法自动清除小说信息的缓存
    }
}
