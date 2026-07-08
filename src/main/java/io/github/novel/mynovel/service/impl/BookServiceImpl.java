package io.github.novel.mynovel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.novel.mynovel.core.annotation.Key;
import io.github.novel.mynovel.core.annotation.Lock;
import io.github.novel.mynovel.core.auth.UserHolder;
import io.github.novel.mynovel.core.common.constant.ErrorCodeEnum;
import io.github.novel.mynovel.core.common.req.PageReqDto;
import io.github.novel.mynovel.core.common.resp.PageRespDto;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.constant.DatabaseConsts;
import io.github.novel.mynovel.dao.entity.*;
import io.github.novel.mynovel.dao.mapper.BookChapterMapper;
import io.github.novel.mynovel.dao.mapper.BookCommentMapper;
import io.github.novel.mynovel.dao.mapper.BookContentMapper;
import io.github.novel.mynovel.dao.mapper.BookInfoMapper;
import io.github.novel.mynovel.dto.AuthorInfoDto;
import io.github.novel.mynovel.dto.req.BookAddReqDto;
import io.github.novel.mynovel.dto.req.ChapterAddReqDto;
import io.github.novel.mynovel.dto.req.ChapterUpdateReqDto;
import io.github.novel.mynovel.dto.req.UserCommentReqDto;
import io.github.novel.mynovel.dto.resp.*;
import io.github.novel.mynovel.manager.cache.*;
import io.github.novel.mynovel.manager.dao.UserDaoManager;
import io.github.novel.mynovel.manager.mq.AmqpMsgManager;
import io.github.novel.mynovel.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookChapterMapper bookChapterMapper;

    private final BookContentMapper bookContentMapper;

    private final BookInfoMapper bookInfoMapper;

    private final BookCommentMapper bookCommentMapper;

    private final AmqpMsgManager amqpMsgManager;

    private final UserDaoManager userDaoManager;

    private final BookCategoryCacheManager bookCategoryCacheManager;

    private final BookInfoCacheManager bookInfoCacheManager;

    private final BookChapterCacheManager bookChapterCacheManager;

    private final BookContentCacheManager bookContentCacheManager;

    private final BookRankCacheManager bookRankCacheManager;

    private final AuthorInfoCacheManager authorInfoCacheManager;

    private final Integer REC_BOOK_COUNT = 4;

    @Override
    public RestResp<List<BookCategoryRespDto>> listCategory(Integer workDirection) {
        return RestResp.ok(bookCategoryCacheManager.listCategory(workDirection));
    }

    @Override
    public RestResp<BookInfoRespDto> getBookById(Long bookId) {
        return RestResp.ok(bookInfoCacheManager.getBookInfo(bookId));
    }

    @Override
    public RestResp<List<BookChapterRespDto>> listChapters(Long bookId) {
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        return RestResp.ok(bookChapterMapper.selectList(queryWrapper).stream()
                .map(v -> BookChapterRespDto.builder()
                        .id(v.getId())
                        .chapterName(v.getChapterName())
                        .isVip(v.getIsVip())
                        .build()).toList());
    }

    @Override
    public RestResp<BookContentAboutRespDto> getBookContentAbout(Long chapterId) {
        // 查询章节信息
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(chapter.getBookId());
        // 查询章节内容
        String bookContent = bookContentCacheManager.getBookContent(chapterId);

        // 组装dto并返回
        BookContentAboutRespDto res = BookContentAboutRespDto.builder()
                .bookInfo(bookInfo)
                .chapterInfo(chapter)
                .bookContent(bookContent)
                .build();

        return RestResp.ok(res);
    }

    @Override
    public RestResp<Long> getPreChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询上一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .lt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
                Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                        .map(BookChapter::getId)
                        .orElse(null)
        );
    }

    @Override
    public RestResp<Long> getNextChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询下一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .gt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
                .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
                Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                        .map(BookChapter::getId)
                        .orElse(null)
        );
    }

    @Override
    public RestResp<List<BookRankRespDto>> listVisitRankBooks() {
        return RestResp.ok(bookRankCacheManager.listVisitRankBooks());
    }

    @Override
    public RestResp<List<BookRankRespDto>> listNewestRankBooks() {
        return RestResp.ok(bookRankCacheManager.listNewestRankBooks());
    }

    @Override
    public RestResp<List<BookRankRespDto>> listUpdateRankBooks() {
        return RestResp.ok(bookRankCacheManager.listUpdateRankBooks());
    }

    @Override
    public RestResp<List<BookInfoRespDto>> listRecBooks(Long bookId) {
        // 获取当前小说的类型
        Long categoryId = bookInfoCacheManager.getBookInfo(bookId).getCategoryId();
        // 获取同类型小说的id列表
        List<Long> lastUpdateIdList = bookInfoCacheManager.getLastUpdateIdList(categoryId);

        // 检查列表是否为空或不足
        if (CollectionUtils.isEmpty(lastUpdateIdList)) {
            return RestResp.ok(Collections.emptyList());
        }

        // 排除当前书籍
        List<Long> candidateIdList = lastUpdateIdList.stream()
                .filter(id -> !id.equals(bookId))
                .toList();

        if (candidateIdList.isEmpty()) {
            return RestResp.ok(Collections.emptyList());
        }

        // 确定实际推荐的书籍数量
        int actualRecCount = Math.min(REC_BOOK_COUNT, candidateIdList.size());

        // 从候选id中随机挑选出指定数量的id
        List<Long> recBookIds = RandomUtil.randomEleList(
                candidateIdList,
                actualRecCount
        );

        List<BookInfoRespDto> respDtoList = recBookIds.stream().map(
                id -> bookInfoCacheManager.getBookInfo(id)
        ).toList();

        return RestResp.ok(respDtoList);
    }

    @Override
    public RestResp<BookChapterAboutRespDto> getLastChapterAbout(Long bookId) {
        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);

        // 查询最新章节信息
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(
                bookInfo.getLastChapterId());

        // 查询章节内容
        String content = bookContentCacheManager.getBookContent(bookInfo.getLastChapterId());

        // 查询章节总数
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId);
        Long chapterTotal = bookChapterMapper.selectCount(chapterQueryWrapper);

        // 组装数据并返回
        return RestResp.ok(BookChapterAboutRespDto.builder()
                .chapterInfo(bookChapter)
                .chapterTotal(chapterTotal)
                .contentSummary(content.substring(0, 30))
                .build());
    }

    @Override
    public RestResp<Void> addVisitCount(Long bookId) {
        LambdaUpdateWrapper<BookInfo> updateWrapper =
                Wrappers.lambdaUpdate();

        updateWrapper.eq(BookInfo::getId, bookId)
                .setSql("visit_count = visit_count + 1");

        bookInfoMapper.update(null, updateWrapper);
        return RestResp.ok();
    }

    @Override
    @Lock(prefix = "userComment", failCode = ErrorCodeEnum.USER_COMMENTED)
    public RestResp<Void> saveComment(@Key(expr = "#{userId + '::' + bookId}") UserCommentReqDto dto) {
        // 校验书籍存在
        BookInfo bookInfo = bookInfoMapper.selectById(dto.getBookId());
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }

        // 校验用户是否已发表评论
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, dto.getUserId())
                .eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, dto.getBookId());
        if (bookCommentMapper.selectCount(queryWrapper) > 0) {
            // 用户已发表评论
            return RestResp.fail(ErrorCodeEnum.USER_COMMENTED);
        }

        BookComment bookComment = BeanUtil.copyProperties(dto, BookComment.class);
        bookComment.setCreateTime(LocalDateTime.now());
        bookComment.setUpdateTime(LocalDateTime.now());
        bookCommentMapper.insert(bookComment);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> deleteComment(Long userId, Long commentId) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), commentId)
                .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, userId);
        bookCommentMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> updateComment(Long userId, Long id, String content) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), id)
                .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, userId);
        BookComment bookComment = new BookComment();
        bookComment.setCommentContent(content);
        bookCommentMapper.update(bookComment, queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<BookCommentRespDto> listNewestComments(Long bookId) {
        // 查询评论总数
        QueryWrapper<BookComment> commentCountQueryWrapper = new QueryWrapper<>();
        commentCountQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId);
        Long commentTotal = bookCommentMapper.selectCount(commentCountQueryWrapper);
        BookCommentRespDto bookCommentRespDto = BookCommentRespDto.builder()
                .commentTotal(commentTotal).build();

        if (commentTotal > 0) {
            // 查询最新评论列表
            QueryWrapper<BookComment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId)
                    .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName())
                    .last(DatabaseConsts.SqlEnum.LIMIT_5.getSql());
            List<BookComment> bookComments = bookCommentMapper.selectList(commentQueryWrapper);

            // 查询评论用户信息，并设置需要返回的评论用户名
            List<Long> userIds = bookComments.stream().map(BookComment::getUserId).toList();
            List<UserInfo> userInfos = userDaoManager.listUsers(userIds);
            Map<Long, UserInfo> userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(UserInfo::getId, Function.identity()));
            List<BookCommentRespDto.CommentInfo> commentInfos = bookComments.stream()
                    .map(v -> BookCommentRespDto.CommentInfo.builder()
                            .id(v.getId())
                            .commentUserId(v.getUserId())
                            .commentUser(userInfoMap.get(v.getUserId()).getUsername())
                            .commentUserPhoto(userInfoMap.get(v.getUserId()).getUserPhoto())
                            .commentContent(v.getCommentContent())
                            .commentTime(v.getCreateTime()).build()).toList();
            bookCommentRespDto.setComments(commentInfos);
    } else {
        bookCommentRespDto.setComments(Collections.emptyList());
    }
        return RestResp.ok(bookCommentRespDto);
    }

    @Override
    public RestResp<PageRespDto<UserCommentRespDto>> listComments(Long userId, PageReqDto pageReqDto) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, userId)
                .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());

        List<BookComment> comments;
        long total;
        if (pageReqDto.isFetchAll()) {
            comments = bookCommentMapper.selectList(queryWrapper);
            total = comments.size();
        } else {
            Page<BookComment> page = new Page<>(pageReqDto.getPageNum(), pageReqDto.getPageSize());
            Page<BookComment> commentPage = bookCommentMapper.selectPage(page, queryWrapper);
            comments = commentPage.getRecords();
            total = commentPage.getTotal();
        }

        if (CollectionUtils.isEmpty(comments)) {
            return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), total,
                    Collections.emptyList()));
        }

        List<Long> bookIds = comments.stream().map(BookComment::getBookId).distinct().toList();
        Map<Long, BookInfo> bookInfoMap = bookInfoMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getId, Function.identity()));

        List<UserCommentRespDto> userComments = comments.stream()
                .map(comment -> {
                    BookInfo bookInfo = bookInfoMap.get(comment.getBookId());
                    return UserCommentRespDto.builder()
                            .commentContent(comment.getCommentContent())
                            .commentBookPic(Optional.ofNullable(bookInfo).map(BookInfo::getPicUrl).orElse(null))
                            .commentBook(Optional.ofNullable(bookInfo).map(BookInfo::getBookName).orElse(null))
                            .commentTime(comment.getCreateTime())
                            .build();
                }).toList();

        return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), total, userComments));
    }

    @Override
    public RestResp<PageRespDto<BookInfoRespDto>> listAuthorBooks(PageReqDto dto) {
        IPage<BookInfo> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();
        query.eq(BookInfo::getAuthorId, UserHolder.getAuthorId())
                .orderByDesc(BookInfo::getCreateTime);
        IPage<BookInfo> bookInfoIPage = bookInfoMapper.selectPage(page, query);

        List<BookInfoRespDto> list = bookInfoIPage.getRecords().stream()
                .map(bookInfo -> BeanUtil.copyProperties(bookInfo, BookInfoRespDto.class))
                .toList();

        return RestResp.ok(
                PageRespDto.of(dto.getPageNum(), dto.getPageSize(), bookInfoIPage.getTotal(), list)
        );
    }

    @Transactional
    @Override
    public RestResp<Void> saveBook(BookAddReqDto dto) {
        // 获取当前作家ID
        Long authorId = UserHolder.getAuthorId();
        if (authorId == null) {
            return RestResp.fail(ErrorCodeEnum.AUTHOR_STATUS_ERROR);
        }

        // 校验作家状态（0-正常，1-封禁）
        AuthorInfoDto authorInfo = authorInfoCacheManager.getAuthor(UserHolder.getUserId());
        if (authorInfo == null || !Objects.equals(authorInfo.getStatus(), 0)) {
            return RestResp.fail(ErrorCodeEnum.AUTHOR_STATUS_ERROR);
        }

        // 校验同一作者下小说名是否已存在
        LambdaQueryWrapper<BookInfo> nameCheckWrapper = Wrappers.lambdaQuery();
        nameCheckWrapper.eq(BookInfo::getAuthorId, authorId)
                .eq(BookInfo::getBookName, dto.getBookName());
        if (bookInfoMapper.selectCount(nameCheckWrapper) > 0) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NAME_EXIST);
        }

        // 构建小说实体
        BookInfo bookInfo = new BookInfo();
        bookInfo.setWorkDirection(dto.getWorkDirection());
        bookInfo.setCategoryId(dto.getCategoryId());
        bookInfo.setCategoryName(dto.getCategoryName());
        bookInfo.setPicUrl(dto.getPicUrl());
        bookInfo.setBookName(dto.getBookName());
        bookInfo.setAuthorId(authorId);
        bookInfo.setAuthorName(authorInfo.getPenName());
        bookInfo.setBookDesc(dto.getBookDesc());
        bookInfo.setScore(0);
        bookInfo.setBookStatus(0);
        bookInfo.setVisitCount(0L);
        bookInfo.setWordCount(0);
        bookInfo.setCommentCount(0);
        bookInfo.setIsVip(dto.getIsVip());
        bookInfo.setCreateTime(LocalDateTime.now());
        bookInfo.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        bookInfoMapper.insert(bookInfo);

        return RestResp.ok();
    }

    @Override
    public RestResp<Void> saveBookChapter(ChapterAddReqDto dto) {
        // 校验小说是否存在且属于当前作家
        BookInfo bookInfo = bookInfoMapper.selectById(dto.getBookId());
        Long authorId = UserHolder.getAuthorId();
        if (bookInfo == null || !bookInfo.getAuthorId().equals(authorId)) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }

        // 获取当前最大章节号
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, dto.getBookId())
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookChapter lastChapter = bookChapterMapper.selectOne(queryWrapper);
        int nextChapterNum = (lastChapter == null) ? 1 : lastChapter.getChapterNum() + 1;

        // 保存章节信息
        BookChapter bookChapter = new BookChapter();
        bookChapter.setBookId(dto.getBookId());
        bookChapter.setChapterNum(nextChapterNum);
        bookChapter.setChapterName(dto.getChapterName());
        bookChapter.setWordCount(dto.getChapterContent().length());
        bookChapter.setIsVip(dto.getIsVip());
        bookChapter.setCreateTime(LocalDateTime.now());
        bookChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.insert(bookChapter);

        // 保存章节内容
        BookContent bookContent = new BookContent();
        bookContent.setChapterId(bookChapter.getId());
        bookContent.setContent(dto.getChapterContent());
        bookContent.setCreateTime(LocalDateTime.now());
        bookContent.setUpdateTime(LocalDateTime.now());
        bookContentMapper.insert(bookContent);

        // 更新小说信息（总字数、最新章节）
        bookInfo.setWordCount(bookInfo.getWordCount() + bookChapter.getWordCount());
        bookInfo.setLastChapterId(bookChapter.getId());
        bookInfo.setLastChapterName(bookChapter.getChapterName());
        bookInfo.setLastChapterUpdateTime(LocalDateTime.now());
        bookInfo.setUpdateTime(LocalDateTime.now());
        bookInfoMapper.updateById(bookInfo);

        // 清除缓存
        bookInfoCacheManager.evictBookInfoCache(dto.getBookId());

        // 发送小说信息更新的mq消息
        amqpMsgManager.sendBookChangeMsg(dto.getBookId());

        return RestResp.ok();
    }

    @Transactional
    @Override
    public RestResp<Void> deleteBookChapter(Long chapterId) {
        // 查询章节信息
        BookChapter bookChapter = bookChapterMapper.selectById(chapterId);
        if (bookChapter == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }

        // 校验小说是否属于当前作家
        BookInfo bookInfo = bookInfoMapper.selectById(bookChapter.getBookId());
        Long authorId = UserHolder.getAuthorId();
        if (bookInfo == null || !bookInfo.getAuthorId().equals(authorId)) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }

        // 删除章节内容
        QueryWrapper<BookContent> contentQueryWrapper = new QueryWrapper<>();
        contentQueryWrapper.eq(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterId);
        bookContentMapper.delete(contentQueryWrapper);

        // 删除章节
        bookChapterMapper.deleteById(chapterId);

        // 更新小说总字数
        bookInfo.setWordCount(Math.max(0, bookInfo.getWordCount() - bookChapter.getWordCount()));
        bookInfo.setUpdateTime(LocalDateTime.now());
        bookInfoMapper.updateById(bookInfo);

        // 清除缓存
        bookChapterCacheManager.evictBookChapterCache(chapterId);
        bookContentCacheManager.evictBookContentCache(chapterId);
        bookInfoCacheManager.evictBookInfoCache(bookChapter.getBookId());

        // 发送小说信息更新的mq消息
        amqpMsgManager.sendBookChangeMsg(bookChapter.getBookId());

        return RestResp.ok();
    }

    @Override
    public RestResp<ChapterContentRespDto> getBookChapter(Long chapterId) {
        // 从缓存获取章节信息
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        // 从缓存获取章节内容
        String content = bookContentCacheManager.getBookContent(chapterId);

        return RestResp.ok(ChapterContentRespDto.builder()
                .chapterName(chapter.getChapterName())
                .chapterContent(content)
                .isVip(chapter.getIsVip())
                .build());
    }

    @Transactional
    @Override
    public RestResp<Void> updateBookChapter(Long chapterId, ChapterUpdateReqDto dto) {
        // 查询章节信息
        BookChapter bookChapter = bookChapterMapper.selectById(chapterId);
        if (bookChapter == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }

        // 校验小说是否属于当前作家
        BookInfo bookInfo = bookInfoMapper.selectById(bookChapter.getBookId());
        Long authorId = UserHolder.getAuthorId();
        if (bookInfo == null || !bookInfo.getAuthorId().equals(authorId)) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }

        // 更新章节信息
        int oldWordCount = bookChapter.getWordCount();
        bookChapter.setChapterName(dto.getChapterName());
        bookChapter.setWordCount(dto.getChapterContent().length());
        bookChapter.setIsVip(dto.getIsVip());
        bookChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.updateById(bookChapter);

        // 更新章节内容
        QueryWrapper<BookContent> contentQueryWrapper = new QueryWrapper<>();
        contentQueryWrapper.eq(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterId);
        BookContent bookContent = bookContentMapper.selectOne(contentQueryWrapper);
        if (bookContent != null) {
            bookContent.setContent(dto.getChapterContent());
            bookContent.setUpdateTime(LocalDateTime.now());
            bookContentMapper.update(bookContent, contentQueryWrapper);
        }

        // 更新小说总字数
        int newWordCount = bookChapter.getWordCount();
        bookInfo.setWordCount(Math.max(0, bookInfo.getWordCount() - oldWordCount + newWordCount));
        bookInfo.setUpdateTime(LocalDateTime.now());
        bookInfoMapper.updateById(bookInfo);

        // 清除缓存
        bookChapterCacheManager.evictBookChapterCache(chapterId);
        bookContentCacheManager.evictBookContentCache(chapterId);
        bookInfoCacheManager.evictBookInfoCache(bookChapter.getBookId());

        // 发送小说信息更新的mq消息
        amqpMsgManager.sendBookChangeMsg(bookChapter.getBookId());

        return RestResp.ok();
    }

    @Override
    public RestResp<PageRespDto<BookChapterRespDto>> listBookChapters(Long bookId, PageReqDto dto) {
        IPage<BookChapter> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        IPage<BookChapter> chapterPage = bookChapterMapper.selectPage(page, queryWrapper);

        List<BookChapterRespDto> list = chapterPage.getRecords().stream()
                .map(chapter -> BookChapterRespDto.builder()
                        .id(chapter.getId())
                        .bookId(chapter.getBookId())
                        .chapterNum(chapter.getChapterNum())
                        .chapterName(chapter.getChapterName())
                        .chapterWordCount(chapter.getWordCount())
                        .chapterUpdateTime(chapter.getUpdateTime())
                        .isVip(chapter.getIsVip())
                        .build())
                .toList();

        return RestResp.ok(
                PageRespDto.of(dto.getPageNum(), dto.getPageSize(), chapterPage.getTotal(), list)
        );
    }

}
