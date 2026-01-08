package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("line_station")
public class LineStation {
    @TableId
    private Long id;
    private Long lineId;
    private Long stationId;
    private Integer seq;

}
