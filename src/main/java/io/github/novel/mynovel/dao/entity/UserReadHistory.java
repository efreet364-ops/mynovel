package io.github.novel.mynovel.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户阅读历史
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 */
@Getter
@Setter
@TableName("user_read_history")
public class UserReadHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 小说ID
     */
    private Long bookId;

    /**
     * 上一次阅读的章节内容表ID
     */
    private Long preContentId;

    /**
     * 创建时间;
     */
    private LocalDateTime createTime;

    /**
     * 更新时间;
     */
    private LocalDateTime updateTime;


}
