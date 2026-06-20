package io.github.novel.mynovel.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 稿费收入明细统计
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 */
@Getter
@Setter
@TableName("author_income_detail")
public class AuthorIncomeDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作家ID
     */
    private Long authorId;

    /**
     * 小说ID;0表示全部作品
     */
    private Long bookId;

    /**
     * 收入日期
     */
    private LocalDate incomeDate;

    /**
     * 订阅总额
     */
    private Integer incomeAccount;

    /**
     * 订阅次数
     */
    private Integer incomeCount;

    /**
     * 订阅人数
     */
    private Integer incomeNumber;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
