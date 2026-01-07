package com.subway.ticket.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.subway.ticket.domain.LineStation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LineStationMapper extends BaseMapper<LineStation> {
}
