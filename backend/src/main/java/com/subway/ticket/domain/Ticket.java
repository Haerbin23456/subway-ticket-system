package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("ticket")
public class Ticket {
    @TableId
    private Long id;
    private Long orderId;
    private Long qrcodeTokenId;
    private java.sql.Timestamp issuedAt;
    private String status;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getQrcodeTokenId() { return qrcodeTokenId; }
    public void setQrcodeTokenId(Long qrcodeTokenId) { this.qrcodeTokenId = qrcodeTokenId; }
    public java.sql.Timestamp getIssuedAt() { return issuedAt; }
    public void setIssuedAt(java.sql.Timestamp issuedAt) { this.issuedAt = issuedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.sql.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
    public java.sql.Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.sql.Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
