package com.subway.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.domain.LineStation;
import com.subway.ticket.domain.Order;
import com.subway.ticket.domain.Station;
import com.subway.ticket.domain.enums.OrderStatus;
import com.subway.ticket.dto.CreateOrderReq;
import com.subway.ticket.exception.BusinessException;
import com.subway.ticket.repository.LineStationMapper;
import com.subway.ticket.repository.OrderMapper;
import com.subway.ticket.repository.StationMapper;
import com.subway.ticket.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final StationMapper stationMapper;
    private final LineStationMapper lineStationMapper;

    private static final BigDecimal BASE_PRICE = new BigDecimal("2.00");
    private static final BigDecimal PER_SEGMENT_PRICE = new BigDecimal("0.50");
    private static final BigDecimal CROSS_LINE_FIXED_PRICE = new BigDecimal("4.00");

    public OrderServiceImpl(OrderMapper orderMapper, StationMapper stationMapper, LineStationMapper lineStationMapper) {
        this.orderMapper = orderMapper;
        this.stationMapper = stationMapper;
        this.lineStationMapper = lineStationMapper;
    }

    @Override
    @Transactional
    public Order createOrder(CreateOrderReq req) {
        Station sFrom = stationMapper.selectOne(new QueryWrapper<Station>().eq("code", req.from).last("limit 1"));
        Station sTo = stationMapper.selectOne(new QueryWrapper<Station>().eq("code", req.to).last("limit 1"));
        
        if (sFrom == null) {
            throw new RuntimeException("出发站不存在: " + req.from);
        }
        if (sTo == null) {
            throw new RuntimeException("到达站不存在: " + req.to);
        }

        BigDecimal price;
        if (!sFrom.getLineId().equals(sTo.getLineId())) {
            // 简化的跨线计费 logic
            price = CROSS_LINE_FIXED_PRICE;
        } else {
            LineStation lsFrom = lineStationMapper.selectOne(new QueryWrapper<LineStation>().eq("station_id", sFrom.getId()).eq("line_id", sFrom.getLineId()).last("limit 1"));
            LineStation lsTo = lineStationMapper.selectOne(new QueryWrapper<LineStation>().eq("station_id", sTo.getId()).eq("line_id", sTo.getLineId()).last("limit 1"));
            
            if (lsFrom == null || lsTo == null) {
                 throw new BusinessException("站点线路信息异常");
            }
            
            int segments = Math.abs(Optional.ofNullable(lsFrom.getSeq()).orElse(0) - Optional.ofNullable(lsTo.getSeq()).orElse(0));
            price = BASE_PRICE.add(PER_SEGMENT_PRICE.multiply(new BigDecimal(segments)));
        }

        Order o = new Order();
        o.setUserId(null); // Anonymous
        o.setFromStationId(sFrom.getId());
        o.setToStationId(sTo.getId());
        o.setPrice(price);
        o.setStatus(OrderStatus.CREATED);
        o.setCreatedAt(Timestamp.from(Instant.now()));
        o.setUpdatedAt(Timestamp.from(Instant.now()));
        
        orderMapper.insert(o);
        return o;
    }
}
