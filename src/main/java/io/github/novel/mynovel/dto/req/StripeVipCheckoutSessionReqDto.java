package io.github.novel.mynovel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StripeVipCheckoutSessionReqDto {

    @Schema(description = "VIP套餐ID")
    @NotNull(message = "VIP套餐ID不能为空")
    private Long productId;
}
