package io.github.novel.mynovel.controller.front;

import io.github.novel.mynovel.core.common.exception.BusinessException;
import io.github.novel.mynovel.core.constant.ApiRouterConsts;
import io.github.novel.mynovel.service.PayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_PAY_URL_PREFIX)
@RequiredArgsConstructor
@Slf4j
public class PayController {

    private final PayService payService;

    /**
     * stripe webhook 回调接口
     * @param payload
     * @param signature
     * @return
     */
    @PostMapping("stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String signature) {
        try {
            payService.handleStripeWebhook(payload, signature);
            return ResponseEntity.ok("ok");
        } catch (BusinessException exception) {
            // Webhook 失败必须返回非 2xx，否则 Stripe 会认为事件已成功投递而不再重试。
            log.warn("Stripe webhook business failure: {}", exception.getMessage());
            return ResponseEntity.badRequest().body("failed");
        } catch (Exception exception) {
            log.error("Stripe webhook system failure", exception);
            return ResponseEntity.internalServerError().body("failed");
        }
    }
}
