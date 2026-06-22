package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.resp.BookCategoryRespDto;

import java.util.List;

/**
 * 小说模块 服务类
 *
 */
public interface BookService {

    /**
     * 小说分类列表查询
     */
    RestResp<List<BookCategoryRespDto>> listCategory(Integer workDirection);
}
