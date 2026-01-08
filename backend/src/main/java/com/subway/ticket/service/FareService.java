package com.subway.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.config.FareProperties;
import com.subway.ticket.domain.Line;
import com.subway.ticket.domain.Station;
import com.subway.ticket.dto.FareQuote;
import com.subway.ticket.dto.RouteStep;
import com.subway.ticket.repository.StationMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FareService {

    private final GraphService graphService;
    private final StationMapper stationMapper;
    private final FareProperties fareProperties;

    public FareService(GraphService graphService, StationMapper stationMapper, FareProperties fareProperties) {
        this.graphService = graphService;
        this.stationMapper = stationMapper;
        this.fareProperties = fareProperties;
    }

    public FareQuote calculateFare(String fromCode, String toCode) {
        if (graphService.isEmpty()) {
            graphService.initGraph();
        }

        Station sFrom = stationMapper.selectList(new QueryWrapper<Station>().eq("code", fromCode).last("limit 1")).stream().findFirst().orElse(null);
        Station sTo = stationMapper.selectList(new QueryWrapper<Station>().eq("code", toCode).last("limit 1")).stream().findFirst().orElse(null);

        if (sFrom == null || sTo == null) {
            return new FareQuote(fromCode, toCode, 0, BigDecimal.ZERO, "STATION_NOT_FOUND", null, null);
        }

        List<Long> startIds = graphService.getIdsByName(sFrom.getName());
        List<Long> endIds = graphService.getIdsByName(sTo.getName());

        if (startIds.isEmpty() || endIds.isEmpty()) {
            return new FareQuote(fromCode, toCode, 0, BigDecimal.ZERO, "NODES_NOT_FOUND", null, null);
        }

        GraphService.PathResult pathResult = graphService.findPath(startIds, endIds);

        if (pathResult == null) {
            return new FareQuote(fromCode, toCode, 0, BigDecimal.ZERO, "UNREACHABLE", null, null);
        }

        int distance = pathResult.distance;
        BigDecimal price = calculatePrice(distance);

        List<String> pathCodes = new ArrayList<>();
        for (Long pid : pathResult.pathIds) {
            String code = graphService.getCodeById(pid);
            if (code != null) pathCodes.add(code);
        }

        List<RouteStep> steps = buildRouteSteps(pathResult.pathIds);

        return new FareQuote(fromCode, toCode, distance, price, "HANGZHOU_RULE", pathCodes, steps);
    }

    private BigDecimal calculatePrice(int distance) {
        // 1. Check configured rules
        if (fareProperties.getRules() != null) {
            for (FareProperties.Rule rule : fareProperties.getRules()) {
                if (distance <= rule.getDistance()) {
                    return rule.getPrice();
                }
            }
        }

        // 2. Check extra distance rule
        FareProperties.ExtraRule extra = fareProperties.getExtra();
        if (extra != null && distance > extra.getStartDistance()) {
            int extraDist = distance - extra.getStartDistance();
            int steps = (int) Math.ceil((double) extraDist / extra.getInterval());
            return extra.getBasePriceForExtra().add(extra.getPricePerInterval().multiply(new BigDecimal(steps)));
        }

        // Fallback
        return fareProperties.getBasePrice() != null ? fareProperties.getBasePrice() : BigDecimal.ZERO;
    }

    private List<RouteStep> buildRouteSteps(List<Long> pathIds) {
        List<RouteStep> steps = new ArrayList<>();
        if (pathIds == null || pathIds.isEmpty()) return steps;

        Long currentLineId = null;
        String startStation = null;
        String endStation;
        int count = 0;

        for (int i = 0; i < pathIds.size(); i++) {
            Long nodeId = pathIds.get(i);
            Long lineId = graphService.getLineIdByNodeId(nodeId);
            String stationName = graphService.getNameById(nodeId);

            if (i == 0) {
                currentLineId = lineId;
                startStation = stationName;
            } else {
                if (!Objects.equals(lineId, currentLineId)) {
                    // Line changed!
                    endStation = graphService.getNameById(pathIds.get(i - 1));
                    steps.add(createRouteStep(currentLineId, startStation, endStation, count));

                    currentLineId = lineId;
                    startStation = stationName;
                    count = 0;
                } else {
                    count++;
                }
            }
        }

        if (startStation != null && count > 0) {
            endStation = graphService.getNameById(pathIds.getLast());
            steps.add(createRouteStep(currentLineId, startStation, endStation, count));
        }

        return steps;
    }

    private RouteStep createRouteStep(Long lineId, String startStation, String endStation, int count) {
        Line line = graphService.getLineInfo(lineId);
        String lName = line != null ? line.getName() : "Unknown Line";
        String lColor = graphService.getLineColor(lName, line != null ? line.getColor() : null);
        return new RouteStep(lName, lColor, startStation, endStation, count);
    }
}
