package io.github.novel.mynovel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.novel.mynovel.core.common.constant.ErrorCodeEnum;
import io.github.novel.mynovel.core.common.exception.BusinessException;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.config.StripeProperties;
import io.github.novel.mynovel.core.constant.DatabaseConsts;
import io.github.novel.mynovel.core.util.PayAmountUtils;
import io.github.novel.mynovel.dao.entity.PayStripe;
import io.github.novel.mynovel.dao.entity.UserPayLog;
import io.github.novel.mynovel.dao.entity.VipProduct;
import io.github.novel.mynovel.dao.mapper.PayStripeMapper;
import io.github.novel.mynovel.dao.mapper.UserInfoMapper;
import io.github.novel.mynovel.dao.mapper.UserPayLogMapper;
import io.github.novel.mynovel.dao.mapper.VipProductMapper;
import io.github.novel.mynovel.dto.req.StripeCheckoutSessionReqDto;
import io.github.novel.mynovel.dto.req.StripeVipCheckoutSessionReqDto;
import io.github.novel.mynovel.dto.resp.StripeCheckoutSessionRespDto;
import io.github.novel.mynovel.dto.resp.StripePayStatusRespDto;
import io.github.novel.mynovel.manager.UserVipManager;
import io.github.novel.mynovel.service.PayService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_FAILED = "FAILED";
    private static final byte PAY_CHANNEL_STRIPE = 2;
    private static final byte PRODUCT_TYPE_COIN = 0;
    private static final byte PRODUCT_TYPE_VIP = 1;
    private static final int VIP_PRODUCT_STATUS_ENABLED = 0;
    private static final DateTimeFormatter ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final StripeProperties stripeProperties;
    private final PayStripeMapper payStripeMapper;
    private final UserInfoMapper userInfoMapper;
    private final UserPayLogMapper userPayLogMapper;
    private final VipProductMapper vipProductMapper;
    private final UserVipManager userVipManager;
    private final ObjectMapper objectMapper;

    /**
     * 创建 Stripe Checkout Session。
     *
     * 业务流程：
     * 1. 校验用户输入的人民币金额；
     * 2. 按固定汇率换算 Stripe 实际收取的 AUD 分值；
     * 3. 先创建本地支付流水，生成可追踪的商户订单号；
     * 4. 调用 Stripe 创建托管支付页；
     * 5. 将 Stripe session 信息回写到本地流水，最后把 checkoutUrl 返回给前端。
     *
     * 这里不做任何余额变更。余额只能由 webhook 在确认支付成功后更新。
     */
    @Transactional
    @Override
    public RestResp<StripeCheckoutSessionRespDto> createStripeCheckoutSession(
            Long userId, StripeCheckoutSessionReqDto dto) {
        requireStripeSecretKey();

        // 金额边界是业务规则的一部分，前端校验只改善体验，后端必须再次校验。
        PayAmountUtils.validateAmountCny(dto.getAmountCny(),
                stripeProperties.getMinAmountCny(), stripeProperties.getMaxAmountCny());

        // Stripe 金额按最小货币单位提交；AUD 的最小单位是 cent。
        Integer amountAudCent = PayAmountUtils.convertCnyToAudCent(
                dto.getAmountCny(), stripeProperties.getCnyToAudRate());
        // 第一版业务规则：人民币金额和屋币数量 1:1，Stripe 仅负责按 AUD 完成收款。
        Integer coinValue = dto.getAmountCny();
        String outTradeNo = generateOutTradeNo();
        LocalDateTime now = LocalDateTime.now();

        // 先落本地订单，再创建 Stripe Session。这样即使用户付款很快、webhook 先于前端跳回到达，
        // 后端也能通过 checkout_session_id 找到本地订单并完成履约。
        PayStripe payStripe = new PayStripe();
        payStripe.setUserId(userId);
        payStripe.setOutTradeNo(outTradeNo);
        payStripe.setStatus(STATUS_CREATED);
        payStripe.setAmountCny(dto.getAmountCny());
        payStripe.setAmountAudCent(amountAudCent);
        payStripe.setCoinValue(coinValue);
        payStripe.setProductType((int) PRODUCT_TYPE_COIN);
        payStripe.setProductId(0L);
        payStripe.setProductName("屋币");
        payStripe.setProductValue(coinValue);
        payStripe.setCreateTime(now);
        payStripe.setUpdateTime(now);
        payStripeMapper.insert(payStripe);

        try {
            Stripe.apiKey = stripeProperties.getSecretKey();
            Session session = Session.create(buildSessionCreateParams(
                    userId, outTradeNo, dto.getAmountCny(), amountAudCent,
                    "屋币充值", dto.getAmountCny() + " CNY = " + coinValue + " 屋币",
                    PRODUCT_TYPE_COIN, 0L, "屋币", coinValue));

            // Stripe Session 创建成功后再回写 sessionId 和 checkoutUrl。
            // 本地状态仍保持 CREATED，等待 webhook 确认支付结果。
            payStripeMapper.markCheckoutCreated(payStripe.getId(), session.getId(), session.getUrl(),
                    LocalDateTime.now());
            return RestResp.ok(StripeCheckoutSessionRespDto.builder()
                    .outTradeNo(outTradeNo)
                    .checkoutUrl(session.getUrl())
                    .amountCny(dto.getAmountCny())
                    .coinValue(coinValue)
                    .productType((int) PRODUCT_TYPE_COIN)
                    .productName("屋币")
                    .productValue(coinValue)
                    .currency(stripeProperties.getCurrency())
                    .amountAud(PayAmountUtils.convertAudCentToAud(amountAudCent))
                    .build());
        } catch (StripeException exception) {
            log.warn("Create Stripe Checkout Session failed, outTradeNo={}", outTradeNo, exception);
            // 本地订单已经创建，但 Stripe 未能创建支付页，标记 FAILED 便于后续排查。
            payStripe.setStatus(STATUS_FAILED);
            payStripe.setUpdateTime(LocalDateTime.now());
            payStripeMapper.updateById(payStripe);
            throw new BusinessException(ErrorCodeEnum.STRIPE_PAY_ERROR);
        }
    }

    @Transactional
    @Override
    public RestResp<StripeCheckoutSessionRespDto> createStripeVipCheckoutSession(
            Long userId, StripeVipCheckoutSessionReqDto dto) {
        requireStripeSecretKey();

        VipProduct product = vipProductMapper.selectById(dto.getProductId());
        if (product == null || !Objects.equals(product.getStatus(), VIP_PRODUCT_STATUS_ENABLED)) {
            throw new BusinessException(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
        }

        Integer amountCny = product.getPriceCent() / 100;
        Integer amountAudCent = PayAmountUtils.convertCnyToAudCent(
                amountCny, stripeProperties.getCnyToAudRate());
        String outTradeNo = generateOutTradeNo();
        LocalDateTime now = LocalDateTime.now();

        PayStripe payStripe = new PayStripe();
        payStripe.setUserId(userId);
        payStripe.setOutTradeNo(outTradeNo);
        payStripe.setStatus(STATUS_CREATED);
        payStripe.setAmountCny(amountCny);
        payStripe.setAmountAudCent(amountAudCent);
        payStripe.setCoinValue(0);
        payStripe.setProductType((int) PRODUCT_TYPE_VIP);
        payStripe.setProductId(product.getId());
        payStripe.setProductName(product.getName());
        payStripe.setProductValue(product.getDurationDays());
        payStripe.setCreateTime(now);
        payStripe.setUpdateTime(now);
        payStripeMapper.insert(payStripe);

        try {
            Stripe.apiKey = stripeProperties.getSecretKey();
            Session session = Session.create(buildSessionCreateParams(
                    userId, outTradeNo, amountCny, amountAudCent,
                    product.getName(), product.getName() + "，有效期 " + product.getDurationDays() + " 天",
                    PRODUCT_TYPE_VIP, product.getId(), product.getName(), product.getDurationDays()));

            payStripeMapper.markCheckoutCreated(payStripe.getId(), session.getId(), session.getUrl(),
                    LocalDateTime.now());
            return RestResp.ok(StripeCheckoutSessionRespDto.builder()
                    .outTradeNo(outTradeNo)
                    .checkoutUrl(session.getUrl())
                    .amountCny(amountCny)
                    .coinValue(0)
                    .productType((int) PRODUCT_TYPE_VIP)
                    .productName(product.getName())
                    .productValue(product.getDurationDays())
                    .currency(stripeProperties.getCurrency())
                    .amountAud(PayAmountUtils.convertAudCentToAud(amountAudCent))
                    .build());
        } catch (StripeException exception) {
            log.warn("Create Stripe VIP Checkout Session failed, outTradeNo={}", outTradeNo, exception);
            payStripe.setStatus(STATUS_FAILED);
            payStripe.setUpdateTime(LocalDateTime.now());
            payStripeMapper.updateById(payStripe);
            throw new BusinessException(ErrorCodeEnum.STRIPE_PAY_ERROR);
        }
    }

    /**
     * 查询当前用户自己的 Stripe 订单状态。
     *
     * outTradeNo 是前端成功页轮询的关键参数。查询时必须同时限定 userId，
     * 避免用户通过猜测订单号读取他人的支付信息。
     */
    @Override
    public RestResp<StripePayStatusRespDto> getStripePayStatus(Long userId, String outTradeNo) {
        QueryWrapper<PayStripe> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo)
                .eq("user_id", userId)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        PayStripe payStripe = payStripeMapper.selectOne(queryWrapper);
        if (payStripe == null) {
            throw new BusinessException(ErrorCodeEnum.PAY_ORDER_NOT_FOUND);
        }

        return RestResp.ok(StripePayStatusRespDto.builder()
                .outTradeNo(payStripe.getOutTradeNo())
                .status(payStripe.getStatus())
                .amountCny(payStripe.getAmountCny())
                .coinValue(payStripe.getCoinValue())
                .productType(payStripe.getProductType())
                .productName(payStripe.getProductName())
                .productValue(payStripe.getProductValue())
                .vipExpireTime(Objects.equals(payStripe.getProductType(), (int) PRODUCT_TYPE_VIP)
                        && Objects.equals(payStripe.getStatus(), STATUS_PAID)
                        ? userVipManager.getVipExpireTime(payStripe.getUserId()) : null)
                .amountAud(PayAmountUtils.convertAudCentToAud(payStripe.getAmountAudCent()))
                .paidTime(payStripe.getPaidTime())
                .build());
    }

    /**
     * 处理 Stripe webhook。
     *
     * Stripe webhook 是充值到账的唯一可信入口：
     * - 必须先用 webhook secret 验签；
     * - 必须确认事件代表支付完成；
     * - 必须在同一个事务内完成订单状态、充值记录和用户余额更新；
     * - 必须可重复执行，因为 Stripe 会在网络异常或非 2xx 响应时重试。
     */
    @Transactional
    @Override
    public void handleStripeWebhook(String payload, String signature) {
        requireStripeWebhookSecret();
        Event event;
        try {
            // constructEvent 会同时解析事件并验证 Stripe-Signature，验签失败不能继续处理。
            event = Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STRIPE_PAY_ERROR);
        }

        try {
            JsonNode eventRoot = objectMapper.readTree(payload);
            JsonNode sessionNode = eventRoot.path("data").path("object");
            String eventType = event.getType();

            if ("checkout.session.completed".equals(eventType)) {
                // completed 不总是代表资金已到账，延迟支付方式会在后续 async 事件里确认。
                if (!"paid".equals(sessionNode.path("payment_status").asText())) {
                    return;
                }
                fulfillPaidSession(event.getId(), sessionNode);
            } else if ("checkout.session.async_payment_succeeded".equals(eventType)) {
                // 对延迟支付方式，最终成功事件到达时再执行入账。
                fulfillPaidSession(event.getId(), sessionNode);
            } else if ("checkout.session.async_payment_failed".equals(eventType)) {
                // 异步支付失败只更新本地订单状态，不产生充值记录，也不调整余额。
                markSessionFailed(event.getId(), sessionNode);
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("Handle Stripe webhook failed, eventId={}", event.getId(), exception);
            throw new BusinessException(ErrorCodeEnum.STRIPE_PAY_ERROR);
        }
    }

    /**
     * 构造 Stripe Checkout Session 参数。
     *
     * successUrl/cancelUrl 用于浏览器跳转体验，不用于判断是否到账。
     * clientReferenceId 和 metadata 用于排查、审计和在 Stripe 控制台反查本地订单。
     */
    private SessionCreateParams buildSessionCreateParams(Long userId, String outTradeNo, Integer amountCny,
                                                         Integer amountAudCent, String stripeProductName,
                                                         String stripeProductDescription, Byte productType,
                                                         Long productId, String productName, Integer productValue) {
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(appendQuery(stripeProperties.getSuccessUrl(),
                        "session_id={CHECKOUT_SESSION_ID}&outTradeNo=" + outTradeNo))
                .setCancelUrl(appendQuery(stripeProperties.getCancelUrl(),
                        "status=cancelled&outTradeNo=" + outTradeNo))
                .setClientReferenceId(outTradeNo)
                // metadata 是 webhook 回查和审计的辅助信息，真正入账仍以本地订单为准。
                .putAllMetadata(Map.of(
                        "userId", String.valueOf(userId),
                        "outTradeNo", outTradeNo,
                        "amountCny", String.valueOf(amountCny),
                        "productType", String.valueOf(productType),
                        "productId", String.valueOf(productId),
                        "productValue", String.valueOf(productValue)
                ))
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(stripeProperties.getCurrency())
                                // Stripe 要求 unitAmount 使用最小货币单位；这里传入的是 AUD cent。
                                .setUnitAmount(Long.valueOf(amountAudCent))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(stripeProductName)
                                        .setDescription(stripeProductDescription)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * 处理已支付 Session 的本地履约。
     *
     * 该方法完成真正的业务入账：
     * 1. 根据 Stripe checkout_session_id 找到本地订单；
     * 2. 判断订单是否已支付，避免重复处理；
     * 3. 条件更新订单为 PAID；
     * 4. 写入 user_pay_log；
     * 5. 原子增加 user_info.account_balance。
     */
    private void fulfillPaidSession(String eventId, JsonNode sessionNode) {
        String checkoutSessionId = sessionNode.path("id").asText(null);
        if (!StringUtils.hasText(checkoutSessionId)) {
            throw new BusinessException(ErrorCodeEnum.STRIPE_PAY_ERROR);
        }

        QueryWrapper<PayStripe> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("checkout_session_id", checkoutSessionId)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        PayStripe payStripe = payStripeMapper.selectOne(queryWrapper);
        if (payStripe == null) {
            throw new BusinessException(ErrorCodeEnum.PAY_ORDER_NOT_FOUND);
        }
        if (Objects.equals(payStripe.getStatus(), STATUS_PAID)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String paymentIntentId = sessionNode.path("payment_intent").asText(null);
        // 条件更新是 webhook 幂等核心：重复事件不会重复写充值记录或增加余额。
        int marked = payStripeMapper.markPaid(payStripe.getId(), checkoutSessionId, paymentIntentId,
                eventId, now, now);
        if (marked == 0) {
            // 其他请求可能已经先一步把订单标记为 PAID，当前 webhook 重放直接结束。
            return;
        }

        if (Objects.equals(payStripe.getProductType(), (int) PRODUCT_TYPE_VIP)) {
            fulfillVip(payStripe, now);
            return;
        }
        fulfillCoin(payStripe, now);
    }

    private void fulfillCoin(PayStripe payStripe, LocalDateTime now) {
        UserPayLog userPayLog = new UserPayLog();
        userPayLog.setUserId(payStripe.getUserId());
        userPayLog.setPayChannel(PAY_CHANNEL_STRIPE);
        userPayLog.setOutTradeNo(payStripe.getOutTradeNo());
        userPayLog.setAmount(payStripe.getAmountCny() * 100);
        userPayLog.setProductType(PRODUCT_TYPE_COIN);
        userPayLog.setProductId(0L);
        userPayLog.setProductName("屋币");
        userPayLog.setProductValue(payStripe.getCoinValue());
        userPayLog.setPayTime(now);
        userPayLog.setCreateTime(now);
        userPayLog.setUpdateTime(now);
        userPayLogMapper.insert(userPayLog);

        // 余额必须使用数据库原子递增，避免并发支付完成时出现读改写覆盖。
        userInfoMapper.increaseAccountBalance(payStripe.getUserId(), payStripe.getCoinValue().longValue());
    }

    private void fulfillVip(PayStripe payStripe, LocalDateTime now) {
        UserPayLog userPayLog = new UserPayLog();
        userPayLog.setUserId(payStripe.getUserId());
        userPayLog.setPayChannel(PAY_CHANNEL_STRIPE);
        userPayLog.setOutTradeNo(payStripe.getOutTradeNo());
        userPayLog.setAmount(payStripe.getAmountCny() * 100);
        userPayLog.setProductType(PRODUCT_TYPE_VIP);
        userPayLog.setProductId(payStripe.getProductId());
        userPayLog.setProductName(payStripe.getProductName());
        userPayLog.setProductValue(payStripe.getProductValue());
        userPayLog.setPayTime(now);
        userPayLog.setCreateTime(now);
        userPayLog.setUpdateTime(now);
        userPayLogMapper.insert(userPayLog);

        userVipManager.openOrRenewVip(payStripe.getUserId(), payStripe.getProductValue());
    }

    /**
     * 标记异步支付失败。
     *
     * SQL 中带有 status != 'PAID' 条件，防止乱序事件把已经入账的订单回退成失败。
     */
    private void markSessionFailed(String eventId, JsonNode sessionNode) {
        String checkoutSessionId = sessionNode.path("id").asText(null);
        if (StringUtils.hasText(checkoutSessionId)) {
            payStripeMapper.markFailedByCheckoutSessionId(checkoutSessionId, eventId, LocalDateTime.now());
        }
    }

    private String generateOutTradeNo() {
        // ST 前缀用于区分 Stripe 订单；时间戳方便人工排查，随机片段降低冲突概率。
        return "ST" + LocalDateTime.now().format(ORDER_TIME_FORMATTER)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String appendQuery(String url, String query) {
        if (!StringUtils.hasText(url)) {
            // success-url/cancel-url 是创建 Checkout Session 的必要配置，缺失时直接暴露配置错误。
            throw new BusinessException(ErrorCodeEnum.PAY_CONFIG_ERROR);
        }
        return url + (url.contains("?") ? "&" : "?") + query;
    }

    private void requireStripeSecretKey() {
        if (!StringUtils.hasText(stripeProperties.getSecretKey())) {
            throw new BusinessException(ErrorCodeEnum.PAY_CONFIG_ERROR);
        }
    }

    private void requireStripeWebhookSecret() {
        if (!StringUtils.hasText(stripeProperties.getWebhookSecret())) {
            throw new BusinessException(ErrorCodeEnum.PAY_CONFIG_ERROR);
        }
    }
}
