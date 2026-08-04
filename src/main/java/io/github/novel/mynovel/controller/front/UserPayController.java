package io.github.novel.mynovel.controller.front;

import io.github.novel.mynovel.core.auth.UserHolder;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.constant.ApiRouterConsts;
import io.github.novel.mynovel.dto.req.StripeCheckoutSessionReqDto;
import io.github.novel.mynovel.dto.resp.StripeCheckoutSessionRespDto;
import io.github.novel.mynovel.dto.resp.StripePayStatusRespDto;
import io.github.novel.mynovel.service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_USER_URL_PREFIX + ApiRouterConsts.PAY_URL_PREFIX)
@RequiredArgsConstructor
public class UserPayController {

    private final PayService payService;

    @Operation(summary = "创建 Stripe Checkout Session")
    @PostMapping("stripe/checkout-session")
    public RestResp<StripeCheckoutSessionRespDto> createStripeCheckoutSession(
            @Valid @RequestBody StripeCheckoutSessionReqDto dto) {
        return payService.createStripeCheckoutSession(UserHolder.getUserId(), dto);
    }

    /**
     * 轮询支付状态接口
     * @param outTradeNo
     * @return
     */
    @Operation(summary = "查询 Stripe 支付状态")
    @GetMapping("stripe/status/{outTradeNo}")
    public RestResp<StripePayStatusRespDto> getStripePayStatus(@PathVariable String outTradeNo) {
        return payService.getStripePayStatus(UserHolder.getUserId(), outTradeNo);
    }
}
