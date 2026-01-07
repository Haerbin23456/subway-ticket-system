package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("payment")
public class Payment {
    @TableId
    private Long id;
    private Long orderId;
    private java.math.BigDecimal amount;
    private String status;
    private java.sql.Timestamp paidAt;
    private String channel;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.sql.Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(java.sql.Timestamp paidAt) { this.paidAt = paidAt; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public java.sql.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
    public java.sql.Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.sql.Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
