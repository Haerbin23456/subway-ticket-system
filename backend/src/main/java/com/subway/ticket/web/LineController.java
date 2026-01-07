package com.subway.ticket.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.domain.Line;
import com.subway.ticket.repository.LineMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LineController {
    private final LineMapper lineMapper;

    public LineController(LineMapper lineMapper) {
        this.lineMapper = lineMapper;
    }

    @GetMapping("/lines")
    public ResponseEntity<List<Line>> lines() {
        List<Line> list = lineMapper.selectList(new QueryWrapper<Line>().eq("is_active", 1));
        return ResponseEntity.ok(list);
    }
}
