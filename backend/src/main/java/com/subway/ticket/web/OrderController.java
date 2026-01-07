package com.subway.ticket.web;

import com.subway.ticket.domain.Order;
import com.subway.ticket.dto.CreateOrderReq;
import com.subway.ticket.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderReq req) {
        return ResponseEntity.ok(orderService.createOrder(req));
    }
}
