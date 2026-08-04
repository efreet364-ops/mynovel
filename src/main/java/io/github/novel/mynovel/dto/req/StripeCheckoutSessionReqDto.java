package io.github.novel.mynovel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StripeCheckoutSessionReqDto {

    @Schema(description = "人民币充值金额，单位：元")
    @NotNull
    @Min(1)
    @Max(500)
    private Integer amountCny;
}
