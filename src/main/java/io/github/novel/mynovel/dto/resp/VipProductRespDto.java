package io.github.novel.mynovel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VipProductRespDto {

    @Schema(description = "套餐ID")
    private Long id;

    @Schema(description = "套餐名")
    private String name;

    @Schema(description = "有效天数")
    private Integer durationDays;

    @Schema(description = "价格，单位分")
    private Integer priceCent;

    @Schema(description = "价格，单位元")
    private Integer priceCny;
}
