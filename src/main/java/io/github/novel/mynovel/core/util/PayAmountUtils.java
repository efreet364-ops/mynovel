package io.github.novel.mynovel.core.util;

import io.github.novel.mynovel.core.common.constant.ErrorCodeEnum;
import io.github.novel.mynovel.core.common.exception.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PayAmountUtils {

    private PayAmountUtils() {
    }

    public static void validateAmountCny(Integer amountCny, Integer minAmountCny, Integer maxAmountCny) {
        if (amountCny == null || amountCny < minAmountCny || amountCny > maxAmountCny) {
            throw new BusinessException(ErrorCodeEnum.PAY_AMOUNT_ERROR);
        }
    }

    public static Integer convertCnyToAudCent(Integer amountCny, BigDecimal cnyToAudRate) {
        // Stripe 要求金额使用最小货币单位，这里将人民币元按固定汇率换算为澳元分。
        return BigDecimal.valueOf(amountCny)
                .multiply(cnyToAudRate)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    public static BigDecimal convertAudCentToAud(Integer amountAudCent) {
        return BigDecimal.valueOf(amountAudCent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY);
    }
}
