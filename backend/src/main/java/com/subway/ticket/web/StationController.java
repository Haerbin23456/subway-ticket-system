package com.subway.ticket.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.domain.Station;
import com.subway.ticket.repository.StationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StationController {
    private final StationMapper stationMapper;

    public StationController(StationMapper stationMapper) {
        this.stationMapper = stationMapper;
    }

    @GetMapping("/stations")
    public ResponseEntity<List<Station>> stations(@RequestParam("lineId") Long lineId) {
        List<Station> list = stationMapper.selectList(new QueryWrapper<Station>().eq("line_id", lineId).eq("is_active", 1).orderByAsc("id"));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stations/all")
    public ResponseEntity<List<Station>> all() {
        List<Station> list = stationMapper.selectList(new QueryWrapper<Station>().eq("is_active", 1).orderByAsc("id"));
        return ResponseEntity.ok(list);
    }
}
