package com.subway.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.subway.ticket.domain.Station;
import com.subway.ticket.repository.StationMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class StationService {

    private final StationMapper stationMapper;

    public StationService(StationMapper stationMapper) {
        this.stationMapper = stationMapper;
    }

    public List<Station> searchStations(String keyword) {
        QueryWrapper<Station> query = new QueryWrapper<>();
        query.eq("is_active", 1);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.like("name", keyword);
            // Limit only when searching to avoid huge result if user spams search
            query.last("LIMIT 50");
        } else {
            // No keyword -> Return ALL stations (removed limit)
        }
        
        List<Station> list = stationMapper.selectList(query);
        
        // Deduplicate by name (since transfer stations have multiple records)
        return list.stream()
                .filter(distinctByKey(Station::getName))
                .collect(Collectors.toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
