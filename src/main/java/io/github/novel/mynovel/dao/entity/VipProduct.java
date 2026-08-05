package io.github.novel.mynovel.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * VIP套餐
 */
@Getter
@Setter
@TableName("vip_product")
public class VipProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * VIP套餐名
     */
    private String name;

    /**
     * 有效天数
     */
    private Integer durationDays;

    /**
     * 价格，单位分
     */
    private Integer priceCent;

    /**
     * 状态;0-启用 1-停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
