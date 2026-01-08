package com.subway.ticket.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "subway.fare")
public class FareProperties {

    private BigDecimal basePrice;
    private int baseDistance;
    private List<Rule> rules;
    private ExtraRule extra;

    @Setter
    @Getter
    public static class Rule {
        private int distance;
        private BigDecimal price;
    }

    @Setter
    @Getter
    public static class ExtraRule {
        private int startDistance;
        private int interval;
        private BigDecimal pricePerInterval;
        private BigDecimal basePriceForExtra; // Price at startDistance
    }
}
