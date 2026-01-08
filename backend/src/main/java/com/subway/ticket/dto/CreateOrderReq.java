package com.subway.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrderReq {
    @NotBlank(message = "出发站不能为空")
    private String from;

    @NotBlank(message = "到达站不能为空")
    private String to;
}
