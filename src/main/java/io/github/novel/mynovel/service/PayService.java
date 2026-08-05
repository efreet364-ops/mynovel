package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.req.StripeCheckoutSessionReqDto;
import io.github.novel.mynovel.dto.req.StripeVipCheckoutSessionReqDto;
import io.github.novel.mynovel.dto.resp.StripeCheckoutSessionRespDto;
import io.github.novel.mynovel.dto.resp.StripePayStatusRespDto;

public interface PayService {

    RestResp<StripeCheckoutSessionRespDto> createStripeCheckoutSession(Long userId,
                                                                       StripeCheckoutSessionReqDto dto);

    RestResp<StripeCheckoutSessionRespDto> createStripeVipCheckoutSession(Long userId,
                                                                          StripeVipCheckoutSessionReqDto dto);

    RestResp<StripePayStatusRespDto> getStripePayStatus(Long userId, String outTradeNo);

    void handleStripeWebhook(String payload, String signature);
}
