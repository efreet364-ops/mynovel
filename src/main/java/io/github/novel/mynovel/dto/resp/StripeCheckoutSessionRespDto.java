package io.github.novel.mynovel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StripeCheckoutSessionRespDto {

    @Schema(description = "商户订单号")
    private String outTradeNo;

    @Schema(description = "Stripe Checkout 跳转地址")
    private String checkoutUrl;

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

    @Schema(description = "Stripe 实收币种")
    private String currency;

    @Schema(description = "Stripe 收款金额，单位：澳元")
    private BigDecimal amountAud;
}
