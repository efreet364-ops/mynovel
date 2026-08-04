package io.github.novel.mynovel.core.util;

import io.github.novel.mynovel.core.common.exception.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PayAmountUtilsTest {

    @Test
    void convertCnyToAudCentUsesConfiguredFixedRate() {
        Assertions.assertEquals(21,
                PayAmountUtils.convertCnyToAudCent(1, new BigDecimal("0.21")));
        Assertions.assertEquals(10500,
                PayAmountUtils.convertCnyToAudCent(500, new BigDecimal("0.21")));
    }

    @Test
    void validateAmountCnyRejectsOutOfRangeAmount() {
        Assertions.assertDoesNotThrow(() -> PayAmountUtils.validateAmountCny(1, 1, 500));
        Assertions.assertDoesNotThrow(() -> PayAmountUtils.validateAmountCny(500, 1, 500));
        Assertions.assertThrows(BusinessException.class,
                () -> PayAmountUtils.validateAmountCny(0, 1, 500));
        Assertions.assertThrows(BusinessException.class,
                () -> PayAmountUtils.validateAmountCny(501, 1, 500));
    }
}
