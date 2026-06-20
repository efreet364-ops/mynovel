package io.github.novel.mynovel.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 小说评论回复
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 */
@Getter
@Setter
@TableName("book_comment_reply")
public class BookCommentReply implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 回复用户ID
     */
    private Long userId;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 审核状态;0-待审核 1-审核通过 2-审核不通过
     */
    private Byte auditStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
