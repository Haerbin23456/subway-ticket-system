package com.subway.ticket.web;

import com.subway.ticket.dto.FareQuote;
import com.subway.ticket.service.FareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @GetMapping("/quote")
    public ResponseEntity<FareQuote> quote(@RequestParam("from") String fromCode, @RequestParam("to") String toCode) {
        FareQuote quote = fareService.calculateFare(fromCode, toCode);
        
        if ("STATION_NOT_FOUND".equals(quote.mode) || "NODES_NOT_FOUND".equals(quote.mode)) {
            return ResponseEntity.badRequest().body(quote);
        }
        
        return ResponseEntity.ok(quote);
    }
}

