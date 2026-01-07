package com.subway.ticket.web;

import com.subway.ticket.domain.Order;
import com.subway.ticket.domain.QrcodeToken;
import com.subway.ticket.repository.OrderMapper;
import com.subway.ticket.repository.QrcodeTokenMapper;
import com.subway.ticket.service.QrSignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class QrcodeController {
    private final OrderMapper orderMapper;
    private final QrcodeTokenMapper qrcodeTokenMapper;
    private final QrSignService qrSignService;

    public QrcodeController(OrderMapper orderMapper, QrcodeTokenMapper qrcodeTokenMapper) {
        this.orderMapper = orderMapper;
        this.qrcodeTokenMapper = qrcodeTokenMapper;
        String secret = System.getenv("QR_SIGNING_SECRET");
        if (secret == null || secret.isEmpty()) secret = "dev-secret";
        this.qrSignService = new QrSignService(secret);
    }

    @GetMapping("/orders/{id}/qrcode")
    public ResponseEntity<QrPayload> qrcode(@PathVariable("id") Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null) return ResponseEntity.notFound().build();
        if (!"PAID".equals(o.getStatus())) return ResponseEntity.badRequest().build();
        String nonce = UUID.randomUUID().toString();
        long expEpoch = Instant.now().plusSeconds(15 * 60).getEpochSecond();
        String data = id + ":" + nonce + ":" + expEpoch;
        String sign = qrSignService.sign(data);
        String payloadJson = String.format("{\"orderId\":%d,\"nonce\":\"%s\",\"exp\":%d,\"sign\":\"%s\"}", id, nonce, expEpoch, sign);
        QrcodeToken t = new QrcodeToken();
        t.setOrderId(id);
        t.setNonce(nonce);
        t.setExpiresAt(Timestamp.from(Instant.ofEpochSecond(expEpoch)));
        t.setSignature(sign);
        t.setPayload(payloadJson);
        t.setCreatedAt(Timestamp.from(Instant.now()));
        qrcodeTokenMapper.insert(t);
        return ResponseEntity.ok(new QrPayload(id, nonce, expEpoch, sign));
    }

    public static class QrPayload {
        public Long orderId;
        public String nonce;
        public long exp;
        public String sign;
        public QrPayload(Long orderId, String nonce, long exp, String sign) {
            this.orderId = orderId;
            this.nonce = nonce;
            this.exp = exp;
            this.sign = sign;
        }
    }
}
