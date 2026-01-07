package com.subway.ticket.dto;

public class RouteStep {
    public String lineName;
    public String lineColor;
    public String fromStation;
    public String toStation;
    public int stationCount;
    
    public RouteStep() {}

    public RouteStep(String lineName, String lineColor, String fromStation, String toStation, int stationCount) {
        this.lineName = lineName;
        this.lineColor = lineColor;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.stationCount = stationCount;
    }
}
