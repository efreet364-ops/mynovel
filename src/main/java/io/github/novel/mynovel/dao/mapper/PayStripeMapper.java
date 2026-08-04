package io.github.novel.mynovel.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.novel.mynovel.dao.entity.PayStripe;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;

public interface PayStripeMapper extends BaseMapper<PayStripe> {

    /**
     * Stripe Checkout Session 创建成功后回写 session 信息。
     * 只允许 CREATED 状态更新，避免覆盖已进入终态的订单。
     */
    int markCheckoutCreated(@Param("id") Long id,
                            @Param("checkoutSessionId") String checkoutSessionId,
                            @Param("checkoutUrl") String checkoutUrl,
                            @Param("updateTime") LocalDateTime updateTime);

    /**
     * 将订单标记为已支付。
     * 返回 1 才能继续写充值记录和增加余额；返回 0 表示订单已被其他 webhook 处理过。
     */
    int markPaid(@Param("id") Long id,
                 @Param("checkoutSessionId") String checkoutSessionId,
                 @Param("paymentIntentId") String paymentIntentId,
                 @Param("stripeEventId") String stripeEventId,
                 @Param("paidTime") LocalDateTime paidTime,
                 @Param("updateTime") LocalDateTime updateTime);

    /**
     * 标记异步支付失败。
     * 已支付订单不能被失败事件覆盖，避免 Stripe 事件乱序导致错误回滚。
     */
    int markFailedByCheckoutSessionId(@Param("checkoutSessionId") String checkoutSessionId,
                                      @Param("stripeEventId") String stripeEventId,
                                      @Param("updateTime") LocalDateTime updateTime);
}
