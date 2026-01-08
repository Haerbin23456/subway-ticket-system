package com.subway.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareQuote {
    private String from;
    private String to;
    private int segments;
    private BigDecimal price;
    private String mode;
    private List<String> path;
    private List<RouteStep> steps; // Detailed route steps
}
