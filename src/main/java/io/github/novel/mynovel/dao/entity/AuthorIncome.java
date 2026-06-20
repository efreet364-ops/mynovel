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
 * 稿费收入统计
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 */
@Getter
@Setter
@TableName("author_income")
public class AuthorIncome implements Serializable {

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
     * 小说ID
     */
    private Long bookId;

    /**
     * 收入月份
     */
    private LocalDate incomeMonth;

    /**
     * 税前收入;单位：分
     */
    private Integer preTaxIncome;

    /**
     * 税后收入;单位：分
     */
    private Integer afterTaxIncome;

    /**
     * 支付状态;0-待支付 1-已支付
     */
    private Byte payStatus;

    /**
     * 稿费确认状态;0-待确认 1-已确认
     */
    private Byte confirmStatus;

    /**
     * 详情
     */
    private String detail;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
