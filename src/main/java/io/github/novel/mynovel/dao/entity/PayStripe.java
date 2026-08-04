package io.github.novel.mynovel.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Stripe 支付流水
 */
@Getter
@Setter
@TableName("pay_stripe")
public class PayStripe implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String outTradeNo;

    private String checkoutSessionId;

    private String paymentIntentId;

    private String checkoutUrl;

    /**
     * 交易状态：CREATED-已创建 PAID-已支付 FAILED-支付失败
     */
    private String status;

    /**
     * 用户输入人民币金额，单位：元
     */
    private Integer amountCny;

    /**
     * Stripe 收款金额，单位：澳元分
     */
    private Integer amountAudCent;

    /**
     * 到账屋币数量
     */
    private Integer coinValue;

    private String stripeEventId;

    private LocalDateTime paidTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
