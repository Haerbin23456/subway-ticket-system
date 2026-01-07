package com.subway.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.domain.Order;
import com.subway.ticket.domain.Station;
import com.subway.ticket.domain.enums.OrderStatus;
import com.subway.ticket.dto.CreateOrderReq;
import com.subway.ticket.dto.FareQuote;
import com.subway.ticket.exception.BusinessException;
import com.subway.ticket.repository.OrderMapper;
import com.subway.ticket.repository.StationMapper;
import com.subway.ticket.service.FareService;
import com.subway.ticket.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final StationMapper stationMapper;
    private final FareService fareService;

    public OrderServiceImpl(OrderMapper orderMapper, StationMapper stationMapper, FareService fareService) {
        this.orderMapper = orderMapper;
        this.stationMapper = stationMapper;
        this.fareService = fareService;
    }

    @Override
    @Transactional
    public Order createOrder(CreateOrderReq req) {
        // 1. Calculate price using the same logic as quote
        FareQuote quote = fareService.calculateFare(req.from, req.to);
        
        if (quote == null || quote.price == null) {
             throw new BusinessException("无法计算票价，请检查站点是否连通");
        }
        
        Station sFrom = stationMapper.selectOne(new QueryWrapper<Station>().eq("code", req.from).last("limit 1"));
        Station sTo = stationMapper.selectOne(new QueryWrapper<Station>().eq("code", req.to).last("limit 1"));
        
        if (sFrom == null) {
            throw new BusinessException("出发站不存在: " + req.from);
        }
        if (sTo == null) {
            throw new BusinessException("到达站不存在: " + req.to);
        }

        Order o = new Order();
        o.setUserId(null); // Anonymous
        o.setFromStationId(sFrom.getId());
        o.setToStationId(sTo.getId());
        o.setPrice(quote.price);
        o.setStatus(OrderStatus.CREATED);
        o.setCreatedAt(LocalDateTime.now());
        o.setUpdatedAt(LocalDateTime.now());
        
        orderMapper.insert(o);
        return o;
    }
}
