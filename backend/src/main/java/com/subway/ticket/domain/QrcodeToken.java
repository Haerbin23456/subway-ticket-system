package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qrcode_token")
public class QrcodeToken {
    @TableId
    private Long id;
    private Long orderId;
    private String nonce;
    private LocalDateTime expiresAt;
    private String signature;
    private String payload;
    private LocalDateTime createdAt;
}
