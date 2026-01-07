package com.subway.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateOrderReq {
    @NotBlank(message = "出发站不能为空")
    public String from;

    @NotBlank(message = "到达站不能为空")
    public String to;
}
