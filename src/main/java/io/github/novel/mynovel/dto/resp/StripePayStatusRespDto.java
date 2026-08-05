package io.github.novel.mynovel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StripePayStatusRespDto {

    @Schema(description = "商户订单号")
    private String outTradeNo;

    @Schema(description = "交易状态：CREATED-已创建 PAID-已支付 FAILED-支付失败")
    private String status;

    @Schema(description = "人民币充值金额，单位：元")
    private Integer amountCny;

    @Schema(description = "到账屋币数量")
    private Integer coinValue;

    @Schema(description = "商品类型;0-屋币 1-VIP")
    private Integer productType;

    @Schema(description = "商品名")
    private String productName;

    @Schema(description = "商品值;屋币数量或VIP天数")
    private Integer productValue;

    @Schema(description = "VIP到期时间")
    private LocalDateTime vipExpireTime;

    @Schema(description = "Stripe 收款金额，单位：澳元")
    private BigDecimal amountAud;

    @Schema(description = "支付完成时间")
    private LocalDateTime paidTime;
}
