package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("qrcode_token")
public class QrcodeToken {
    @TableId
    private Long id;
    private Long orderId;
    private String nonce;
    private java.sql.Timestamp expiresAt;
    private String signature;
    private String payload;
    private java.sql.Timestamp createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }
    public java.sql.Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(java.sql.Timestamp expiresAt) { this.expiresAt = expiresAt; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public java.sql.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
}
