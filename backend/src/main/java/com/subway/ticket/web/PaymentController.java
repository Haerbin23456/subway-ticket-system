package com.subway.ticket.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.domain.Order;
import com.subway.ticket.domain.Payment;
import com.subway.ticket.domain.enums.OrderStatus;
import com.subway.ticket.repository.OrderMapper;
import com.subway.ticket.repository.PaymentMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    public PaymentController(PaymentMapper paymentMapper, OrderMapper orderMapper) {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
    }

    @PostMapping("/mock")
    public ResponseEntity<Payment> mock(@RequestBody MockPayReq req) {
        Order o = orderMapper.selectById(req.orderId);
        if (o == null) return ResponseEntity.badRequest().build();
        Payment p = new Payment();
        p.setOrderId(o.getId());
        p.setAmount(o.getPrice() != null ? o.getPrice() : new BigDecimal("0"));
        p.setStatus("SUCCESS");
        p.setPaidAt(Timestamp.from(Instant.now()));
        p.setChannel("MOCK");
        p.setCreatedAt(Timestamp.from(Instant.now()));
        paymentMapper.insert(p);
        o.setStatus(OrderStatus.PAID);
        orderMapper.updateById(o);
        return ResponseEntity.ok(p);
    }

    public static class MockPayReq {
        public Long orderId;
    }
}
