package com.subway.ticket.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.subway.ticket.domain.Station;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StationMapper extends BaseMapper<Station> {
}
