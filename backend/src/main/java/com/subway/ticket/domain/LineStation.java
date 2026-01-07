package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("line_station")
public class LineStation {
    @TableId
    private Long id;
    private Long lineId;
    private Long stationId;
    private Integer seq;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }
    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }
    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }
}
