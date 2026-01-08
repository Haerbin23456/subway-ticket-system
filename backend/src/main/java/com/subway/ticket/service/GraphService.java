package com.subway.ticket.service;

import com.subway.ticket.domain.Line;
import com.subway.ticket.domain.LineStation;
import com.subway.ticket.domain.Station;
import com.subway.ticket.repository.LineMapper;
import com.subway.ticket.repository.LineStationMapper;
import com.subway.ticket.repository.StationMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphService {

    private final StationMapper stationMapper;
    private final LineStationMapper lineStationMapper;
    private final LineMapper lineMapper;

    // Graph representation: (LineStation ID) -> List of Connected (LineStation IDs)
    private final Map<Long, Set<Long>> graph = new HashMap<>();
    
    // LineStation ID -> Station Name (for display & transfer detection)
    private final Map<Long, String> nodeNames = new HashMap<>();
    
    // Station Name -> List of LineStation IDs (for finding start/end nodes)
    private final Map<String, List<Long>> nameToNodeIds = new HashMap<>();
    
    // LineStation ID -> Station Code (for result mapping)
    private final Map<Long, String> nodeToCode = new HashMap<>();

    // LineStation ID -> Line ID (to check if same line)
    private final Map<Long, Long> nodeToLineId = new HashMap<>();
    
    // Station ID -> List of LineStation IDs (to detect transfers via Station ID)
    private final Map<Long, List<Long>> stationIdToLineStationIds = new HashMap<>();
    
    // Line ID -> Line Info (Name, Color)
    private final Map<Long, Line> lineInfoMap = new HashMap<>();

    // Mock colors map for fallback/better visibility
    private final Map<String, String> mockLineColors = new HashMap<>();

    // Configurable weights for Dijkstra
    // Injected via application.properties or defaulted here
    @org.springframework.beans.factory.annotation.Value("${subway.graph.cost-per-station:100}")
    private int costPerStation;
    
    @org.springframework.beans.factory.annotation.Value("${subway.graph.cost-per-transfer:350}")
    private int costPerTransfer;

    public GraphService(StationMapper stationMapper, LineStationMapper lineStationMapper, LineMapper lineMapper) {
        this.stationMapper = stationMapper;
        this.lineStationMapper = lineStationMapper;
        this.lineMapper = lineMapper;
        initMockColors();
    }

    private void initMockColors() {
        mockLineColors.put("1号线", "#DF4749"); // Red
        mockLineColors.put("2号线", "#E57B46"); // Orange
        mockLineColors.put("3号线", "#FFCD00"); // Yellow
        mockLineColors.put("4号线", "#6CB53F"); // Green
        mockLineColors.put("5号线", "#39A4A6"); // Cyan
        mockLineColors.put("6号线", "#2C6BA8"); // Blue
        mockLineColors.put("7号线", "#895E9A"); // Purple
        mockLineColors.put("8号线", "#D32E52"); // Dark Red
        mockLineColors.put("9号线", "#B24C38"); // Brown
        mockLineColors.put("10号线", "#C79F25"); // Gold
        mockLineColors.put("16号线", "#FFAD7C"); // Light Orange
        mockLineColors.put("19号线", "#4298B5"); // Light Blue
    }

    @PostConstruct
    public void initGraph() {
        // Load Line Info
        List<Line> lines = lineMapper.selectList(null);
        lineInfoMap.clear();
        for (Line l : lines) {
            lineInfoMap.put(l.getId(), l);
        }
        
        // Always build from LineStation (DB Sequence)
        buildGraphFromLineStations();
    }
    
    private void buildGraphFromLineStations() {
        System.out.println("GraphService: Building Graph from LineStations...");
        List<Station> stations = stationMapper.selectList(null);
        List<LineStation> lineStations = lineStationMapper.selectList(null);

        graph.clear();
        nodeNames.clear();
        nameToNodeIds.clear();
        nodeToCode.clear();
        nodeToLineId.clear();
        stationIdToLineStationIds.clear();

        // 1. Build Lookup Maps
        Map<Long, Station> stationMap = stations.stream().collect(Collectors.toMap(Station::getId, s -> s));
        
        for (LineStation ls : lineStations) {
            Long nodeId = ls.getId(); // Use LineStation.ID as Node ID
            Station s = stationMap.get(ls.getStationId());
            
            if (s != null) {
                graph.put(nodeId, new HashSet<>());
                nodeNames.put(nodeId, s.getName());
                nameToNodeIds.computeIfAbsent(s.getName(), k -> new ArrayList<>()).add(nodeId);
                nodeToCode.put(nodeId, s.getCode());
                nodeToLineId.put(nodeId, ls.getLineId());
                
                stationIdToLineStationIds.computeIfAbsent(ls.getStationId(), k -> new ArrayList<>()).add(nodeId);
            }
        }

        // 2. Add Same-Line Edges (Physical Track)
        Map<Long, List<LineStation>> byLine = lineStations.stream()
                .collect(Collectors.groupingBy(LineStation::getLineId));

        for (List<LineStation> line : byLine.values()) {
            line.sort(Comparator.comparingInt(LineStation::getSeq));
            for (int i = 0; i < line.size() - 1; i++) {
                Long id1 = line.get(i).getId();
                Long id2 = line.get(i + 1).getId();
                
                if (graph.containsKey(id1) && graph.containsKey(id2)) {
                    graph.get(id1).add(id2);
                    graph.get(id2).add(id1);
                }
            }
        }

        // 3. Add Transfer Edges
        for (List<Long> lsIds : stationIdToLineStationIds.values()) {
            if (lsIds.size() > 1) connectAll(lsIds);
        }
        for (List<Long> lsIds : nameToNodeIds.values()) {
            if (lsIds.size() > 1) connectAll(lsIds);
        }
        
        System.out.println("GraphService: Graph built with " + graph.size() + " nodes.");
    }
    
    private void connectAll(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                Long id1 = ids.get(i);
                Long id2 = ids.get(j);
                
                if (!Objects.equals(nodeToLineId.get(id1), nodeToLineId.get(id2))) {
                    graph.get(id1).add(id2);
                    graph.get(id2).add(id1);
                }
            }
        }
    }
    
    public boolean isTransfer(Long id1, Long id2) {
        Long l1 = nodeToLineId.get(id1);
        Long l2 = nodeToLineId.get(id2);
        return !Objects.equals(l1, l2);
    }
    
    // Public Accessors
    public List<Long> getIdsByName(String name) {
        return nameToNodeIds.getOrDefault(name, Collections.emptyList());
    }
    
    public String getCodeById(Long id) {
        return nodeToCode.get(id);
    }
    
    public String getNameById(Long id) {
        return nodeNames.get(id);
    }
    
    public Long getLineIdByNodeId(Long id) {
        return nodeToLineId.get(id);
    }
    
    public Line getLineInfo(Long lineId) {
        return lineInfoMap.get(lineId);
    }
    
    public String getLineColor(String lineName, String dbColor) {
        if (lineName == null) return dbColor != null ? ensureHashPrefix(dbColor) : "#999";
        
        // Extract main name, e.g. "3号线 (石马-星桥)" -> "3号线"
        String mainName = lineName;
        int parenIdx = lineName.indexOf(" (");
        if (parenIdx > 0) {
            mainName = lineName.substring(0, parenIdx);
        } else {
            // Also check for simple parenthesis without space
            parenIdx = lineName.indexOf("(");
            if (parenIdx > 0) {
                mainName = lineName.substring(0, parenIdx);
            }
        }
        mainName = mainName.trim();

        if (mockLineColors.containsKey(mainName)) {
            return mockLineColors.get(mainName);
        }
        
        return dbColor != null ? ensureHashPrefix(dbColor) : "#999";
    }

    private String ensureHashPrefix(String color) {
        if (color == null || color.isEmpty()) return "#999";
        if (color.startsWith("#")) return color;
        // Check if it's a valid hex string (3 or 6 chars)
        if (color.matches("^[0-9a-fA-F]{3,6}$")) {
            return "#" + color;
        }
        return color;
    }
    
    public boolean isEmpty() {
        return graph.isEmpty();
    }

    // Dijkstra Algorithm
    public PathResult findPath(List<Long> startIds, List<Long> endIds) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
        Map<Long, Integer> dist = new HashMap<>();
        Map<Long, Long> parent = new HashMap<>();
        
        for (Long startId : startIds) {
            pq.add(new Node(startId, 0));
            dist.put(startId, 0);
            parent.put(startId, null);
        }
        
        Set<Long> endIdSet = new HashSet<>(endIds);
        
        while(!pq.isEmpty()) {
            Node current = pq.poll();
            Long u = current.id;
            
            if (current.cost > dist.getOrDefault(u, Integer.MAX_VALUE)) continue;
            
            if (endIdSet.contains(u)) {
                // Found! Reconstruct.
                List<Long> path = new ArrayList<>();
                Long curr = u;
                while(curr != null) {
                    path.add(curr);
                    curr = parent.get(curr);
                }
                Collections.reverse(path);
                
                int stationCount = 0;
                for (int i = 0; i < path.size() - 1; i++) {
                     if (!isTransfer(path.get(i), path.get(i+1))) {
                         stationCount++;
                     }
                }
                
                return new PathResult(stationCount, path);
            }
            
            if (graph.containsKey(u)) {
                for (Long v : graph.get(u)) {
                    boolean isTransfer = isTransfer(u, v);
                    // Use configurable costs
                    int weight = isTransfer ? costPerTransfer : costPerStation;
                    int newCost = current.cost + weight;
                    
                    if (newCost < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                        dist.put(v, newCost);
                        parent.put(v, u);
                        pq.add(new Node(v, newCost));
                    }
                }
            }
        }
        return null;
    }
    
    private static class Node {
        Long id;
        int cost;
        public Node(Long id, int cost) { this.id = id; this.cost = cost; }
    }
    
    public static class PathResult {
        public int distance;
        public List<Long> pathIds;
        public PathResult(int d, List<Long> p) { this.distance = d; this.pathIds = p; }
    }
}
