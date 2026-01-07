package com.subway.ticket.web;

import com.subway.ticket.domain.Station;
import com.subway.ticket.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StationController {
    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping("/stations")
    public ResponseEntity<List<Station>> stations(@RequestParam("lineId") Long lineId) {
        return ResponseEntity.ok(stationService.getStationsByLine(lineId));
    }

    @GetMapping("/stations/all")
    public ResponseEntity<List<Station>> all() {
        return ResponseEntity.ok(stationService.getAllStations());
    }
    
    @GetMapping("/stations/search")
    public ResponseEntity<List<Station>> search(@RequestParam(value = "keyword", required = false) String keyword) {
        return ResponseEntity.ok(stationService.searchStations(keyword));
    }
}
