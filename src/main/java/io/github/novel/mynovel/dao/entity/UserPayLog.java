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
 * 用户充值记录
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 */
@Getter
@Setter
@TableName("user_pay_log")
public class UserPayLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 充值用户ID
     */
    private Long userId;

    /**
     * 充值方式;0-支付宝 1-微信
     */
    private Byte payChannel;

    /**
     * 商户订单号
     */
    private String outTradeNo;

    /**
     * 充值金额;单位：分
     */
    private Integer amount;

    /**
     * 充值商品类型;0-屋币 1-包年VIP
     */
    private Byte productType;

    /**
     * 充值商品ID
     */
    private Long productId;

    /**
     * 充值商品名;示例值：屋币
     */
    private String productName;

    /**
     * 充值商品值;示例值：255
     */
    private Integer productValue;

    /**
     * 充值时间
     */
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
