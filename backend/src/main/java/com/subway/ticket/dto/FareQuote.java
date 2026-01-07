package com.subway.ticket.dto;

import java.math.BigDecimal;
import java.util.List;

public class FareQuote {
    public String from;
    public String to;
    public int segments;
    public BigDecimal price;
    public String mode;
    public List<String> path;
    public List<RouteStep> steps; // Detailed route steps
    
    public FareQuote() {}

    public FareQuote(String from, String to, int segments, BigDecimal price, String mode, List<String> path, List<RouteStep> steps) {
        this.from = from;
        this.to = to;
        this.segments = segments;
        this.price = price;
        this.mode = mode;
        this.path = path;
        this.steps = steps;
    }
}
