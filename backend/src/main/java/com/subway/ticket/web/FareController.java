package com.subway.ticket.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.domain.LineStation;
import com.subway.ticket.domain.Station;
import com.subway.ticket.repository.LineStationMapper;
import com.subway.ticket.repository.StationMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fares")
public class FareController {
    private final StationMapper stationMapper;
    private final LineStationMapper lineStationMapper;

    // Graph representation: Station ID -> List of Connected Station IDs
    private Map<Long, Set<Long>> graph = new HashMap<>();
    // Station ID -> Station Name (for transfer detection)
    private Map<Long, String> stationNames = new HashMap<>();
    // Station Name -> List of Station IDs (for transfer detection)
    private Map<String, List<Long>> nameToIds = new HashMap<>();

    public FareController(StationMapper stationMapper, LineStationMapper lineStationMapper) {
        this.stationMapper = stationMapper;
        this.lineStationMapper = lineStationMapper;
    }

    @PostConstruct
    public void initGraph() {
        // Use the injected edges if available, otherwise build from DB seq (legacy fallback)
        if (this.injectedEdges != null && !this.injectedEdges.isEmpty()) {
            buildGraphFromEdges();
        } else {
            buildGraphFromSeq();
        }
    }
    
    // Allow DataInitializer to inject real physical edges
    private List<Map.Entry<String, String>> injectedEdges;
    
    public void setPhysicalEdges(List<Map.Entry<String, String>> edges) {
        this.injectedEdges = edges;
        initGraph();
    }

    private void buildGraphFromEdges() {
        System.out.println("Building Graph from Physical Edges (" + injectedEdges.size() + " edges)...");
        graph.clear();
        stationNames.clear();
        nameToIds.clear();
        
        List<Station> stations = stationMapper.selectList(null);
        // Map Code -> ID
        Map<String, Long> codeToId = new HashMap<>();
        
        for (Station s : stations) {
            graph.put(s.getId(), new HashSet<>());
            stationNames.put(s.getId(), s.getName());
            nameToIds.computeIfAbsent(s.getName(), k -> new ArrayList<>()).add(s.getId());
            codeToId.put(s.getCode(), s.getId());
        }
        
        for (Map.Entry<String, String> edge : injectedEdges) {
            Long id1 = codeToId.get(edge.getKey());
            Long id2 = codeToId.get(edge.getValue());
            
            if (id1 != null && id2 != null) {
                graph.get(id1).add(id2);
                graph.get(id2).add(id1);
            }
        }
        
        // Add Transfer Edges
        addTransferEdges();
        System.out.println("Graph built from edges with " + graph.size() + " nodes.");
    }

    private void buildGraphFromSeq() {
        System.out.println("Building Graph from DB Sequence...");
        List<Station> stations = stationMapper.selectList(null);
        List<LineStation> lineStations = lineStationMapper.selectList(null);

        graph.clear();
        stationNames.clear();
        nameToIds.clear();

        // 1. Initialize Nodes and Name Mapping
        for (Station s : stations) {
            graph.put(s.getId(), new HashSet<>());
            stationNames.put(s.getId(), s.getName());
            nameToIds.computeIfAbsent(s.getName(), k -> new ArrayList<>()).add(s.getId());
        }

        // 2. Add Same-Line Edges (Physical Track)
        // Group LineStations by LineId
        Map<Long, List<LineStation>> byLine = lineStations.stream()
                .collect(Collectors.groupingBy(LineStation::getLineId));

        for (List<LineStation> line : byLine.values()) {
            // Sort by sequence
            line.sort(Comparator.comparingInt(LineStation::getSeq));
            for (int i = 0; i < line.size() - 1; i++) {
                Long id1 = line.get(i).getStationId();
                Long id2 = line.get(i + 1).getStationId();
                if (graph.containsKey(id1) && graph.containsKey(id2)) {
                    graph.get(id1).add(id2);
                    graph.get(id2).add(id1);
                }
            }
        }

        // 3. Add Transfer Edges (Same Name)
        addTransferEdges();
        System.out.println("Graph built from DB seq with " + graph.size() + " nodes.");
    }
    
    private void addTransferEdges() {
        for (List<Long> ids : nameToIds.values()) {
            if (ids.size() > 1) {
                // Connect all stations with same name
                for (int i = 0; i < ids.size(); i++) {
                    for (int j = i + 1; j < ids.size(); j++) {
                        Long id1 = ids.get(i);
                        Long id2 = ids.get(j);
                        graph.get(id1).add(id2);
                        graph.get(id2).add(id1);
                    }
                }
            }
        }
    }

    @GetMapping("/quote")
    public ResponseEntity<FareQuote> quote(@RequestParam("from") String fromCode, @RequestParam("to") String toCode) {
        // Refresh graph if empty (safety check)
        if (graph.isEmpty()) {
            initGraph();
        }

        Station sFrom = stationMapper.selectList(new QueryWrapper<Station>().eq("code", fromCode).last("limit 1")).stream().findFirst().orElse(null);
        Station sTo = stationMapper.selectList(new QueryWrapper<Station>().eq("code", toCode).last("limit 1")).stream().findFirst().orElse(null);

        if (sFrom == null || sTo == null) {
            return ResponseEntity.badRequest().body(new FareQuote(fromCode, toCode, 0, BigDecimal.ZERO, "STATION_NOT_FOUND", null));
        }

        // Use Dijkstra for better path quality (Least Transfers priority)
        PathResult pathResult = dijkstra(sFrom.getId(), sTo.getId());

        if (pathResult == null) {
             return ResponseEntity.ok(new FareQuote(fromCode, toCode, 0, BigDecimal.ZERO, "UNREACHABLE", null));
        }

        int distance = pathResult.distance; 
        BigDecimal price;
        
        if (distance <= 2) { 
             price = new BigDecimal("2.00");
        } else if (distance <= 4) {
             price = new BigDecimal("3.00");
        } else if (distance <= 7) {
             price = new BigDecimal("4.00");
        } else if (distance <= 12) {
             price = new BigDecimal("5.00");
        } else if (distance <= 16) {
             price = new BigDecimal("6.00");
        } else {
             int extra = distance - 16;
             int steps = (int) Math.ceil((double) extra / 4);
             price = new BigDecimal("6.00").add(new BigDecimal("1.00").multiply(new BigDecimal(steps)));
        }
        
        // Convert Path IDs back to Codes for frontend
        List<String> pathCodes = new ArrayList<>();
        
        // Simple lookup: Find codes for IDs. 
        if (idToCode.isEmpty()) {
             List<Station> allStations = stationMapper.selectList(null);
             for(Station s : allStations) {
                 idToCode.put(s.getId(), s.getCode());
             }
        }
        
        for (Long pid : pathResult.pathIds) {
            String code = idToCode.get(pid);
            if (code != null) pathCodes.add(code);
        }
        
        return ResponseEntity.ok(new FareQuote(fromCode, toCode, distance, price, "HANGZHOU_RULE", pathCodes));
    }

    // Revised BFS with 0-1 logic for Transfers
    private int bfs_optimized(Long startId, Long endId) {
         Deque<Long> deque = new ArrayDeque<>();
         deque.add(startId);
         
         Map<Long, Integer> dist = new HashMap<>();
         dist.put(startId, 0); // Distance 0 stations traveled
         
         while(!deque.isEmpty()) {
             Long u = deque.pollFirst();
             int d = dist.get(u);
             
             if (u.equals(endId)) return d; // Returns number of hops (edges)
             
             if (graph.containsKey(u)) {
                 for (Long v : graph.get(u)) {
                     boolean isTransfer = isTransfer(u, v);
                     int weight = isTransfer ? 0 : 1;
                     
                     if (!dist.containsKey(v) || dist.get(v) > d + weight) {
                         dist.put(v, d + weight);
                         if (weight == 0) {
                             deque.addFirst(v);
                         } else {
                             deque.addLast(v);
                         }
                     }
                 }
             }
         }
         return -1;
    }

    // Revised BFS with 0-1 logic for Transfers (effectively Dijkstra for unweighted/0-1 graphs)
    // Priority: Transfer (0 cost) > Physical Move (1 cost)
    private PathResult bfs_with_path(Long startId, Long endId) {
         // Use Deque for 0-1 BFS
         Deque<Long> deque = new ArrayDeque<>();
         deque.add(startId);
         
         Map<Long, Integer> dist = new HashMap<>();
         Map<Long, Long> parent = new HashMap<>(); 
         
         dist.put(startId, 0);
         parent.put(startId, null);
         
         while(!deque.isEmpty()) {
             Long u = deque.pollFirst();
             int d = dist.get(u);
             
             if (u.equals(endId)) {
                 // Reconstruct path
                 List<Long> path = new ArrayList<>();
                 Long curr = endId;
                 while(curr != null) {
                     path.add(curr);
                     curr = parent.get(curr);
                 }
                 Collections.reverse(path);
                 return new PathResult(d, path);
             }
             
             if (graph.containsKey(u)) {
                 for (Long v : graph.get(u)) {
                     boolean isTransfer = isTransfer(u, v);
                     // Adjust weight: Physical move = 1, Transfer = 0 (prefer transfer to reach same logical point)
                     // BUT, if we want to minimize transfers, we should penalize them?
                     // Standard BFS finds shortest path by edges.
                     // If Transfer is 0 weight, algorithm will use infinite transfers if needed (cost free).
                     // Real world: Transfer cost time/effort.
                     // Let's set Physical = 1, Transfer = 0. This minimizes STATIONS passed.
                     // Problem: "Same stations count, but more transfers".
                     // Example: A->B->C (2 stops, 0 transfer) vs A->D->E (2 stops, 1 transfer at D).
                     // If both are 2 stops, BFS picks whichever it finds first.
                     
                     // To penalize transfers slightly without affecting price (based on stations),
                     // we can use a small weight for transfer, e.g. 0.1?
                     // But BFS only works with integer weights or 0/1.
                     // For "Least Transfers" priority when station count is equal, we need a custom cost.
                     // Cost = Stations * 1000 + Transfers * 1.
                     
                     // Implementation: Dijkstra with custom weight.
                     // Weight: Physical = 1000, Transfer = 1.
                     // This way, 1 station (1000) is always more expensive than 999 transfers.
                     // So it still finds shortest path by stations, but breaks ties with fewest transfers.
                     
                     int weight = isTransfer ? 1 : 1000;
                     
                     // Dijkstra-like relaxation
                     if (!dist.containsKey(v) || dist.get(v) > d + weight) {
                         dist.put(v, d + weight);
                         parent.put(v, u);
                         
                         // For 0-1 BFS (or approximate), if weight is small, add to front?
                         // With weights 1 and 1000, we can't use simple 0-1 Deque effectively.
                         // We should use PriorityQueue (Dijkstra).
                         // Or simply: Since we are using standard BFS queue now, let's switch to PriorityQueue.
                     }
                 }
             }
         }
         // If standard BFS failed (shouldn't happen for connected graph), return null
         return null;
    }
    
    // Switch to Dijkstra for better path quality
    private PathResult dijkstra(Long startId, Long endId) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
        pq.add(new Node(startId, 0));
        
        Map<Long, Integer> dist = new HashMap<>();
        Map<Long, Long> parent = new HashMap<>();
        
        dist.put(startId, 0);
        parent.put(startId, null);
        
        while(!pq.isEmpty()) {
            Node current = pq.poll();
            Long u = current.id;
            
            if (current.cost > dist.getOrDefault(u, Integer.MAX_VALUE)) continue;
            
            if (u.equals(endId)) {
                // Found! Reconstruct.
                List<Long> path = new ArrayList<>();
                Long curr = endId;
                while(curr != null) {
                    path.add(curr);
                    curr = parent.get(curr);
                }
                Collections.reverse(path);
                
                // Calculate real station count (ignore transfer costs)
                // Cost = Stations * 1000 + Transfers
                int stationCount = current.cost / 1000;
                return new PathResult(stationCount, path);
            }
            
            if (graph.containsKey(u)) {
                for (Long v : graph.get(u)) {
                    boolean isTransfer = isTransfer(u, v);
                    // Weight: Physical = 1000 (1 station), Transfer = 1 (penalty)
                    int weight = isTransfer ? 1 : 1000;
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
    
    private boolean isTransfer(Long id1, Long id2) {
        String n1 = stationNames.get(id1);
        String n2 = stationNames.get(id2);
        return n1 != null && n1.equals(n2);
    }
    
    private Map<Long, String> idToCode = new HashMap<>();
    
    private static class PathResult {
        int distance;
        List<Long> pathIds;
        public PathResult(int d, List<Long> p) { this.distance = d; this.pathIds = p; }
    }

    public static class FareQuote {
        public String from;
        public String to;
        public int segments;
        public BigDecimal price;
        public String mode;
        public List<String> path; // Changed from List<Long> to List<String>
        
        public FareQuote(String from, String to, int segments, BigDecimal price, String mode, List<String> path) {
            this.from = from;
            this.to = to;
            this.segments = segments;
            this.price = price;
            this.mode = mode;
            this.path = path;
        }
    }
}
