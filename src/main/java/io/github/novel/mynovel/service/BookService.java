package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.resp.BookCategoryRespDto;
import io.github.novel.mynovel.dto.resp.BookChapterRespDto;
import io.github.novel.mynovel.dto.resp.BookContentAboutRespDto;
import io.github.novel.mynovel.dto.resp.BookInfoRespDto;

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

    /**
     * 小说信息查询
     */
    RestResp<BookInfoRespDto> getBookById(Long bookId);

    /**
     * 小说章节列表查询
     */
    RestResp<List<BookChapterRespDto>> listChapters(Long bookId);

    /**
     * 小说内容相关信息查询
     *
     * @param chapterId 章节ID
     * @return 内容相关联的信息
     */
    RestResp<BookContentAboutRespDto> getBookContentAbout(Long chapterId);

    /**
     * 获取上一章节ID
     *
     * @param chapterId 章节ID
     * @return 上一章节ID
     */
    RestResp<Long> getPreChapterId(Long chapterId);

    /**
     * 获取下一章节ID
     *
     * @param chapterId 章节ID
     * @return 下一章节ID
     */
    RestResp<Long> getNextChapterId(Long chapterId);
}
