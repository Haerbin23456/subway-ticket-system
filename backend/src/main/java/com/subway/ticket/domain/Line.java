package com.subway.ticket.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("line")
public class Line {
    @TableId
    private Long id;
    private String name;
    private String color;
    private String code;
    private Integer isActive;

}
