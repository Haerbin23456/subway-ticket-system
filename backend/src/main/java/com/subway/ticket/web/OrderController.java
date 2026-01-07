package com.subway.ticket.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.domain.LineStation;
import com.subway.ticket.domain.Order;
import com.subway.ticket.domain.Station;
import com.subway.ticket.repository.LineStationMapper;
import com.subway.ticket.repository.OrderMapper;
import com.subway.ticket.repository.StationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderMapper orderMapper;
    private final StationMapper stationMapper;
    private final LineStationMapper lineStationMapper;

    public OrderController(OrderMapper orderMapper, StationMapper stationMapper, LineStationMapper lineStationMapper) {
        this.orderMapper = orderMapper;
        this.stationMapper = stationMapper;
        this.lineStationMapper = lineStationMapper;
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody CreateOrderReq req) {
        Station sFrom = stationMapper.selectOne(new QueryWrapper<Station>().eq("code", req.from).last("limit 1"));
        Station sTo = stationMapper.selectOne(new QueryWrapper<Station>().eq("code", req.to).last("limit 1"));
        if (sFrom == null || sTo == null) return ResponseEntity.badRequest().build();
        BigDecimal price;
        if (!sFrom.getLineId().equals(sTo.getLineId())) {
            price = new BigDecimal("4.00");
        } else {
            LineStation lsFrom = lineStationMapper.selectOne(new QueryWrapper<LineStation>().eq("station_id", sFrom.getId()).eq("line_id", sFrom.getLineId()).last("limit 1"));
            LineStation lsTo = lineStationMapper.selectOne(new QueryWrapper<LineStation>().eq("station_id", sTo.getId()).eq("line_id", sTo.getLineId()).last("limit 1"));
            if (lsFrom == null || lsTo == null) {
                return ResponseEntity.badRequest().build();
            }
            int segments = Math.abs(Optional.ofNullable(lsFrom.getSeq()).orElse(0) - Optional.ofNullable(lsTo.getSeq()).orElse(0));
            price = new BigDecimal("2.00").add(new BigDecimal("0.50").multiply(new BigDecimal(segments)));
        }
        Order o = new Order();
        o.setUserId(null);
        o.setFromStationId(sFrom.getId());
        o.setToStationId(sTo.getId());
        o.setPrice(price);
        o.setStatus("CREATED");
        o.setCreatedAt(Timestamp.from(Instant.now()));
        o.setUpdatedAt(Timestamp.from(Instant.now()));
        orderMapper.insert(o);
        return ResponseEntity.ok(o);
    }

    public static class CreateOrderReq {
        public String from;
        public String to;
    }
}
