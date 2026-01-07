package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket")
public class Ticket {
    @TableId
    private Long id;
    private Long orderId;
    private Long qrcodeTokenId;
    private LocalDateTime issuedAt;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
