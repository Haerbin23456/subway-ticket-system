package com.subway.ticket.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subway.ticket.domain.Line;
import com.subway.ticket.domain.LineStation;
import com.subway.ticket.domain.Station;
import com.subway.ticket.repository.*;
import com.subway.ticket.web.FareController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final FareController fareController;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(LineMapper lineMapper, StationMapper stationMapper, LineStationMapper lineStationMapper,
                           OrderMapper orderMapper, TicketMapper ticketMapper, PaymentMapper paymentMapper, 
                           QrcodeTokenMapper qrcodeTokenMapper, FareController fareController, JdbcTemplate jdbcTemplate) {
        this.lineMapper = lineMapper;
        this.stationMapper = stationMapper;
        this.lineStationMapper = lineStationMapper;
        this.orderMapper = orderMapper;
        this.ticketMapper = ticketMapper;
        this.paymentMapper = paymentMapper;
        this.qrcodeTokenMapper = qrcodeTokenMapper;
        this.fareController = fareController;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // FIX: Ensure column size is enough
        // Removed runtime ALTER TABLE. Schema should be correct in schema.sql.

        // Changed to Hangzhou Subway JSON
        String jsonPath = "c:\\src\\subway-ticket-system\\backend\\src\\main\\resources\\hangzhou_subway.json";
        File file = new File(jsonPath);
        if (!file.exists()) {
            System.out.println("JSON file not found at " + jsonPath + ". Skipping import.");
            return;
        }

        try {
            System.out.println("Starting data import from " + jsonPath);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(file);
            // Hangzhou JSON structure: root -> "l" (array of lines)
            JsonNode linesNode = root.path("l");

            if (!linesNode.isArray()) {
                System.out.println("Invalid JSON format: 'l' field is missing or not an array.");
                return;
            }

            // Clear existing data (Order must be cleared first due to FK)
            System.out.println("Clearing existing data...");
            ticketMapper.delete(null);
            paymentMapper.delete(null);
            qrcodeTokenMapper.delete(null);
            orderMapper.delete(null);
            lineStationMapper.delete(null);
            stationMapper.delete(null);
            lineMapper.delete(null);

            List<Map.Entry<String, String>> physicalEdges = new ArrayList<>();

            // Process each Line
            for (JsonNode lineNode : linesNode) {
                // Line Info
                String lineName = lineNode.path("ln").asText(); // "1号线"
                String lineCode = lineNode.path("ls").asText(); // "330100023133" (Line ID?) or just use lineName as code
                String lineColor = lineNode.path("cl").asText(); // "color"
                if (lineColor == null || lineColor.isEmpty()) lineColor = "#1a4695"; // Default

                // If lineCode is complex, maybe generate a simple one
                // Hangzhou JSON: "ls" is like "330100023133", "kn" is "1"
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
                     // If duplicate, append a suffix or skip
                     // Actually, if duplicate, it might mean the JSON has split lines?
                     // Let's just update or skip. For now, we skip to avoid crash.
                     System.out.println("Skipping duplicate line code: " + lineCode);
                     continue; 
                }
                
                lineMapper.insert(line);

                System.out.println("Importing Line: " + lineName);

                // Stations
                JsonNode stationsNode = lineNode.path("st");
                if (stationsNode.isArray()) {
                    int seq = 1;
                    String prevStationCode = null;

                    for (JsonNode stNode : stationsNode) {
                        String stName = stNode.path("n").asText();
                        String stId = stNode.path("sid").asText(); // Unique ID
                        // Coordinate: "sl": "120.234391,30.167585"
                        
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

                        // Build Physical Edge (based on sequence in JSON)
                        if (prevStationCode != null) {
                            physicalEdges.add(new AbstractMap.SimpleEntry<>(prevStationCode, stId));
                        }
                        prevStationCode = stId;
                    }
                }
            }

            System.out.println("Data import completed successfully.");
            
            // Inject physical edges to FareController
            fareController.setPhysicalEdges(physicalEdges);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLineName(String code) {
        Pattern p = Pattern.compile("cc(\\d+)");
        Matcher m = p.matcher(code);
        if (m.find()) {
            return m.group(1) + "号线";
        }
        return code;
    }

    private List<String> sortNodes(List<JsonNode> edges) {
        // Build UNDIRECTED adjacency list
        Map<String, List<String>> adj = new HashMap<>();
        Set<String> allNodes = new HashSet<>();

        for (JsonNode edge : edges) {
            String source = edge.path("source").asText();
            String target = edge.path("target").asText();
            
            adj.computeIfAbsent(source, k -> new ArrayList<>()).add(target);
            adj.computeIfAbsent(target, k -> new ArrayList<>()).add(source);
            
            allNodes.add(source);
            allNodes.add(target);
        }

        if (allNodes.isEmpty()) return new ArrayList<>();

        // 1. Find endpoints (Degree == 1)
        List<String> endpoints = new ArrayList<>();
        for (String node : allNodes) {
            if (adj.getOrDefault(node, Collections.emptyList()).size() == 1) {
                endpoints.add(node);
            }
        }

        // 2. Choose start node
        String startNode;
        if (!endpoints.isEmpty()) {
            // Pick the first endpoint as start
            startNode = endpoints.get(0);
        } else {
            // No endpoints? It might be a circle (Degree usually 2). Pick any node.
            startNode = allNodes.iterator().next();
        }

        // 3. Traverse (DFS/BFS) to order nodes
        // Since it's a line (or line with branches), DFS works well to find a path.
        // For simple lines, just follow the "next" unvisited node.
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        Stack<String> stack = new Stack<>();
        stack.push(startNode);
        
        // This simple traversal follows one path. 
        // If there are branches (Y-shape), it picks one branch and ignores the other until backtracking?
        // But for subway lines stored as sequence, we usually want the "Main Line".
        // Let's just do a simple traversal.
        
        while (!stack.isEmpty()) {
            String current = stack.pop();
            
            if (visited.contains(current)) continue;
            visited.add(current);
            path.add(current);
            
            List<String> neighbors = adj.get(current);
            if (neighbors != null) {
                for (String neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        stack.push(neighbor);
                        // For a simple line, there's only one unvisited neighbor usually.
                        // If we push multiple, we might jump around.
                        // Ideally we should pick ONE and continue, DFS-like but linear.
                        // Actually, for a visual line, we want continuous path.
                        // Let's break after pushing one to prioritize depth (continuity).
                        // BUT, if we break, we might miss the other branch entirely if we don't backtrack properly.
                        // Given we want a "List" sequence, let's just greedily follow one path.
                        // The unvisited branches will be left behind (or added later if we loop).
                        // Let's stick to standard DFS but try to keep order.
                    }
                }
            }
        }
        
        // If the graph was disconnected (multiple segments), we need to find unvisited nodes and repeat.
        // But usually a Line Code (e.g. "1号线") should be connected.
        // If disconnected, just append the next segment.
        for (String node : allNodes) {
            if (!visited.contains(node)) {
                 // Start a new traversal from this unvisited node
                 // Ideally find an endpoint of this new segment
                 String subStart = node;
                 // Optimization: Try to find endpoint of this component
                 // (Skip for simplicity, just start from 'node')
                 
                 Stack<String> subStack = new Stack<>();
                 subStack.push(subStart);
                 while(!subStack.isEmpty()) {
                     String curr = subStack.pop();
                     if(visited.contains(curr)) continue;
                     visited.add(curr);
                     path.add(curr);
                     List<String> nbs = adj.get(curr);
                     if(nbs != null) {
                         for(String n : nbs) {
                             if(!visited.contains(n)) subStack.push(n);
                         }
                     }
                 }
            }
        }

        return path;
    }
}
