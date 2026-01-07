package com.subway.ticket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "subway.fare")
public class FareProperties {

    private BigDecimal basePrice;
    private int baseDistance;
    private List<Rule> rules;
    private ExtraRule extra;

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public int getBaseDistance() {
        return baseDistance;
    }

    public void setBaseDistance(int baseDistance) {
        this.baseDistance = baseDistance;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public ExtraRule getExtra() {
        return extra;
    }

    public void setExtra(ExtraRule extra) {
        this.extra = extra;
    }

    public static class Rule {
        private int distance;
        private BigDecimal price;

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }

    public static class ExtraRule {
        private int startDistance;
        private int interval;
        private BigDecimal pricePerInterval;
        private BigDecimal basePriceForExtra; // Price at startDistance

        public int getStartDistance() {
            return startDistance;
        }

        public void setStartDistance(int startDistance) {
            this.startDistance = startDistance;
        }

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public BigDecimal getPricePerInterval() {
            return pricePerInterval;
        }

        public void setPricePerInterval(BigDecimal pricePerInterval) {
            this.pricePerInterval = pricePerInterval;
        }
        
        public BigDecimal getBasePriceForExtra() {
            return basePriceForExtra;
        }

        public void setBasePriceForExtra(BigDecimal basePriceForExtra) {
            this.basePriceForExtra = basePriceForExtra;
        }
    }
}
