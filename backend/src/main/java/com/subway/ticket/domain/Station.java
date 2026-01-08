package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("station")
public class Station {
    @TableId
    private Long id;
    private String name;
    private Long lineId;
    private String code;
    private Double lng;
    private Double lat;
    private Integer isActive;

    @TableField(exist = false)
    private String lineName;

    @TableField(exist = false)
    private String lineColor;
}
