package com.subway.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStep {
    private String lineName;
    private String lineColor;
    private String fromStation;
    private String toStation;
    private int stationCount;
}
