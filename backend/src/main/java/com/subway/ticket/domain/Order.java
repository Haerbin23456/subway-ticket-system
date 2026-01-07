package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.subway.ticket.domain.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId
    private Long id;
    private Long userId;
    private Long fromStationId;
    private Long toStationId;
    private java.math.BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
