package io.github.novel.mynovel.controller.front;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.constant.ApiRouterConsts;
import io.github.novel.mynovel.dto.resp.VipProductRespDto;
import io.github.novel.mynovel.service.VipService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_URL_PREFIX + "/vip")
@RequiredArgsConstructor
public class VipController {

    private final VipService vipService;

    @Operation(summary = "查询VIP套餐列表")
    @GetMapping("products")
    public RestResp<List<VipProductRespDto>> listProducts() {
        return vipService.listProducts();
    }
}
