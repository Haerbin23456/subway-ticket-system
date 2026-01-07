package com.subway.ticket.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.domain.Order;
import com.subway.ticket.domain.QrcodeToken;
import com.subway.ticket.domain.Ticket;
import com.subway.ticket.domain.enums.OrderStatus;
import com.subway.ticket.repository.OrderMapper;
import com.subway.ticket.repository.QrcodeTokenMapper;
import com.subway.ticket.repository.TicketMapper;
import com.subway.ticket.service.QrSignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;

@RestController
@RequestMapping("/api")
public class KioskController {
    private final OrderMapper orderMapper;
    private final QrcodeTokenMapper qrcodeTokenMapper;
    private final TicketMapper ticketMapper;
    private final QrSignService qrSignService;

    public KioskController(OrderMapper orderMapper, QrcodeTokenMapper qrcodeTokenMapper, TicketMapper ticketMapper) {
        this.orderMapper = orderMapper;
        this.qrcodeTokenMapper = qrcodeTokenMapper;
        this.ticketMapper = ticketMapper;
        String secret = System.getenv("QR_SIGNING_SECRET");
        if (secret == null || secret.isEmpty()) secret = "dev-secret";
        this.qrSignService = new QrSignService(secret);
    }

    @PostMapping("/kiosk/validate")
    public ResponseEntity<ValidateResp> validate(@RequestBody QrPayload qr) {
        String data = qr.orderId + ":" + qr.nonce + ":" + qr.exp;
        if (!qrSignService.verify(data, qr.sign)) {
            return ResponseEntity.ok(new ValidateResp(false, "SIGN_INVALID"));
        }
        if (qr.exp <= Instant.now().getEpochSecond()) {
            return ResponseEntity.ok(new ValidateResp(false, "EXPIRED"));
        }
        Order o = orderMapper.selectById(qr.orderId);
        if (o == null) return ResponseEntity.ok(new ValidateResp(false, "ORDER_NOT_FOUND"));
        if (OrderStatus.PAID != o.getStatus()) return ResponseEntity.ok(new ValidateResp(false, "ORDER_NOT_PAID"));
        QrcodeToken t = qrcodeTokenMapper.selectOne(new QueryWrapper<QrcodeToken>().eq("order_id", qr.orderId).eq("nonce", qr.nonce).eq("signature", qr.sign).last("limit 1"));
        if (t == null) return ResponseEntity.ok(new ValidateResp(false, "TOKEN_NOT_FOUND"));
        return ResponseEntity.ok(new ValidateResp(true, "OK"));
    }

    @PostMapping("/tickets/issue")
    public ResponseEntity<IssueResp> issue(@RequestBody QrPayload qr) {
        Order o = orderMapper.selectById(qr.orderId);
        if (o == null) return ResponseEntity.badRequest().build();
        if (OrderStatus.COMPLETED == o.getStatus()) return ResponseEntity.ok(new IssueResp(true));
        QrcodeToken t = qrcodeTokenMapper.selectOne(new QueryWrapper<QrcodeToken>().eq("order_id", qr.orderId).eq("nonce", qr.nonce).last("limit 1"));
        if (t == null) return ResponseEntity.badRequest().build();
        Ticket ticket = new Ticket();
        ticket.setOrderId(o.getId());
        ticket.setQrcodeTokenId(t.getId());
        ticket.setIssuedAt(Timestamp.from(Instant.now()));
        ticket.setStatus("DISPENSED");
        ticket.setCreatedAt(Timestamp.from(Instant.now()));
        ticket.setUpdatedAt(Timestamp.from(Instant.now()));
        ticketMapper.insert(ticket);
        o.setStatus(OrderStatus.COMPLETED);
        orderMapper.updateById(o);
        return ResponseEntity.ok(new IssueResp(true));
    }

    public static class QrPayload {
        public Long orderId;
        public String nonce;
        public long exp;
        public String sign;
    }

    public static class ValidateResp {
        public boolean valid;
        public String reason;
        public ValidateResp(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }
    }

    public static class IssueResp {
        public boolean issued;
        public IssueResp(boolean issued) { this.issued = issued; }
    }
}
