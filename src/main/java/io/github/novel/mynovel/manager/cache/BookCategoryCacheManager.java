package io.github.novel.mynovel.manager.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.constant.CacheConsts;
import io.github.novel.mynovel.core.constant.DatabaseConsts;
import io.github.novel.mynovel.dao.entity.BookCategory;
import io.github.novel.mynovel.dao.mapper.BookCategoryMapper;
import io.github.novel.mynovel.dto.resp.BookCategoryRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 小说分类 缓存管理类
 */
@Component
@RequiredArgsConstructor
public class BookCategoryCacheManager {

    private final BookCategoryMapper bookCategoryMapper;

    @Cacheable(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER
                , value = CacheConsts.BOOK_CATEGORY_LIST_CACHE_NAME)
    public List<BookCategoryRespDto> listCategory(Integer workDirection) {
        QueryWrapper<BookCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCategoryTable.COLUMN_WORK_DIRECTION, workDirection);

        return bookCategoryMapper.selectList(queryWrapper).stream()
                .map(bookCategory ->
                    BookCategoryRespDto.builder()
                            .id(bookCategory.getId())
                            .name(bookCategory.getName())
                            .build()
                ).toList();
    }
}
