package com.subway.ticket.service;

import com.subway.ticket.domain.Order;
import com.subway.ticket.dto.CreateOrderReq;

public interface OrderService {
    Order createOrder(CreateOrderReq req);
}
