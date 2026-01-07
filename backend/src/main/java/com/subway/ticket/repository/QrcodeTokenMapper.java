package com.subway.ticket.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.subway.ticket.domain.QrcodeToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QrcodeTokenMapper extends BaseMapper<QrcodeToken> {
}
