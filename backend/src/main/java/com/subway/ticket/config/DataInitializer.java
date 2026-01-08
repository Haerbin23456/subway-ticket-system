package com.subway.ticket.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subway.ticket.domain.Line;
import com.subway.ticket.domain.LineStation;
import com.subway.ticket.domain.Station;
import com.subway.ticket.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("db")
public class DataInitializer implements CommandLineRunner {
    private final LineMapper lineMapper;
    private final StationMapper stationMapper;
    private final LineStationMapper lineStationMapper;
    private final OrderMapper orderMapper;
    private final TicketMapper ticketMapper;
    private final PaymentMapper paymentMapper;
    private final QrcodeTokenMapper qrcodeTokenMapper;

    public DataInitializer(LineMapper lineMapper, StationMapper stationMapper, LineStationMapper lineStationMapper,
                           OrderMapper orderMapper, TicketMapper ticketMapper, PaymentMapper paymentMapper, 
                           QrcodeTokenMapper qrcodeTokenMapper) {
        this.lineMapper = lineMapper;
        this.stationMapper = stationMapper;
        this.lineStationMapper = lineStationMapper;
        this.orderMapper = orderMapper;
        this.ticketMapper = ticketMapper;
        this.paymentMapper = paymentMapper;
        this.qrcodeTokenMapper = qrcodeTokenMapper;
    }

    @Override
    public void run(String... args) {
        // Load from classpath
        String jsonFile = "hangzhou_subway.json";
        org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource(jsonFile);
        if (!resource.exists()) {
            log.warn("JSON file not found in classpath: {}. Skipping import.", jsonFile);
            return;
        }

        try {
            log.info("Starting data import from classpath: {}", jsonFile);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resource.getInputStream());
            // Hangzhou JSON structure: root -> "l" (array of lines)
            JsonNode linesNode = root.path("l");

            if (!linesNode.isArray()) {
                log.error("Invalid JSON format: 'l' field is missing or not an array.");
                return;
            }

            // Clear existing data (Order must be cleared first due to FK)
            log.info("Clearing existing data...");
            ticketMapper.delete(null);
            paymentMapper.delete(null);
            qrcodeTokenMapper.delete(null);
            orderMapper.delete(null);
            lineStationMapper.delete(null);
            stationMapper.delete(null);
            lineMapper.delete(null);

            // Process each Line
            for (JsonNode lineNode : linesNode) {
                // Line Info
                String lineName = lineNode.path("ln").asText(); // "1号线"
                String lineCode = lineNode.path("ls").asText(); // "330100023133"
                String lineColor = lineNode.path("cl").asText(); // "color"
                if (lineColor == null || lineColor.isEmpty()) lineColor = "#1a4695"; // Default

                // If lineCode is complex, maybe generate a simple one
                String simpleCode = lineNode.path("kn").asText(); // "1"
                if (simpleCode != null && !simpleCode.isEmpty()) {
                    lineCode = simpleCode;
                }

                Line line = new Line();
                line.setName(lineName);
                line.setCode(lineCode);
                line.setColor(lineColor);
                line.setIsActive(1);
                
                // Check for duplicate line code
                QueryWrapper<Line> query = new QueryWrapper<>();
                query.eq("code", lineCode);
                if (lineMapper.selectCount(query) > 0) {
                     log.warn("Skipping duplicate line code: {}", lineCode);
                     continue; 
                }
                
                lineMapper.insert(line);
                log.info("Importing Line: {}", lineName);

                // Stations
                JsonNode stationsNode = lineNode.path("st");
                if (stationsNode.isArray()) {
                    int seq = 1;
                    for (JsonNode stNode : stationsNode) {
                        String stName = stNode.path("n").asText();
                        String stId = stNode.path("sid").asText(); // Unique ID
                        
                        // Create Station Record
                        Station station = new Station();
                        station.setName(stName);
                        station.setLineId(line.getId());
                        station.setCode(stId);
                        station.setIsActive(1);
                        stationMapper.insert(station);

                        // Link Line-Station
                        LineStation ls = new LineStation();
                        ls.setLineId(line.getId());
                        ls.setStationId(station.getId());
                        ls.setSeq(seq++);
                        lineStationMapper.insert(ls);
                    }
                }
            }

            log.info("Data import completed successfully.");
            log.info("Total Lines: {}", lineMapper.selectCount(null));
            log.info("Total Stations: {}", stationMapper.selectCount(null));
            log.info("Total LineStations: {}", lineStationMapper.selectCount(null));
            
        } catch (Exception e) {
            log.error("Data initialization failed", e);
        }
    }
}
