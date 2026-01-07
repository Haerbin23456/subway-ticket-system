package com.subway.ticket.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.subway.ticket.domain.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
