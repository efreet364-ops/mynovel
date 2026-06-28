package io.github.novel.mynovel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.novel.mynovel.core.common.req.PageReqDto;
import io.github.novel.mynovel.core.common.resp.PageRespDto;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.req.UserCommentReqDto;
import io.github.novel.mynovel.dto.resp.*;
import jakarta.validation.Valid;

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

    /**
     * 小说点击榜查询
     *
     * @return 小说点击排行列表
     */
    RestResp<List<BookRankRespDto>> listVisitRankBooks();

    /**
     * 小说新书榜查询
     *
     * @return 小说新书排行列表
     */
    RestResp<List<BookRankRespDto>> listNewestRankBooks();

    /**
     * 小说更新榜查询
     *
     * @return 小说更新排行列表
     */
    RestResp<List<BookRankRespDto>> listUpdateRankBooks();

    /**
     * 小说相关推荐列表查询
     *
     * @return 小说相关推荐列表
     */
    RestResp<List<BookInfoRespDto>> listRecBooks(Long bookId);

    /**
     * 小说最新章节相关信息查询接口
     */
    RestResp<BookChapterAboutRespDto> getLastChapterAbout(Long bookId);

    /**
     * 增加小说点击量接口
     */
    RestResp<Void> addVisitCount(Long bookId);

    /**
     * 发表评论
     *
     * @param dto 评论相关 DTO
     * @return void
     */
    RestResp<Void> saveComment(@Valid UserCommentReqDto dto);

    /**
     * 删除评论
     *
     * @param userId    评论用户ID
     * @param commentId 评论ID
     * @return void
     */
    RestResp<Void> deleteComment(Long userId, Long commentId);

    /**
     * 修改评论
     *
     * @param userId  用户ID
     * @param id      评论ID
     * @param content 修改后的评论内容
     * @return void
     */
    RestResp<Void> updateComment(Long userId, Long id, String content);

    /**
     * 小说最新评论查询
     *
     * @param bookId 小说ID
     * @return 小说最新评论数据
     */
    RestResp<BookCommentRespDto> listNewestComments(Long bookId);

    /**
     * 分页查询评论
     * @param userId
     * @param pageReqDto
     * @return
     */
    RestResp<PageRespDto<UserCommentRespDto>> listComments(Long userId, PageReqDto pageReqDto);
}
