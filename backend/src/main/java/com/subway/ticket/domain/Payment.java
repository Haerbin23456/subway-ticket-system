package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId
    private Long id;
    private Long orderId;
    private java.math.BigDecimal amount;
    private String status;
    private LocalDateTime paidAt;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
