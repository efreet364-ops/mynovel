package io.github.novel.mynovel.core.config;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private String secretKey;

    private String webhookSecret;

    private String currency = "aud";

    private BigDecimal cnyToAudRate = new BigDecimal("0.21");

    private Integer minAmountCny = 1;

    private Integer maxAmountCny = 500;

    private String successUrl;

    private String cancelUrl;
}
