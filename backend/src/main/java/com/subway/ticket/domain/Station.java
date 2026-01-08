package com.subway.ticket.domain;

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
    private Integer isActive;

}
