package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.PageRespDto;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.req.BookSearchReqDto;
import io.github.novel.mynovel.dto.resp.BookInfoRespDto;

public interface SearchService {

    /**
     * 小说搜索
     *
     * @param condition 搜索条件
     * @return 搜索结果
     */
    RestResp<PageRespDto<BookInfoRespDto>> searchBooks(BookSearchReqDto condition);
}
