package com.subway.ticket.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.subway.ticket.domain.Line;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LineMapper extends BaseMapper<Line> {
}
