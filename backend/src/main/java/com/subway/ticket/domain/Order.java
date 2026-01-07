package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.subway.ticket.domain.enums.OrderStatus;

@TableName("orders")
public class Order {
    @TableId
    private Long id;
    private Long userId;
    private Long fromStationId;
    private Long toStationId;
    private java.math.BigDecimal price;
    private OrderStatus status;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getFromStationId() { return fromStationId; }
    public void setFromStationId(Long fromStationId) { this.fromStationId = fromStationId; }
    public Long getToStationId() { return toStationId; }
    public void setToStationId(Long toStationId) { this.toStationId = toStationId; }
    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public java.sql.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
    public java.sql.Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.sql.Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
