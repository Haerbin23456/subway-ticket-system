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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FareController.class);
    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @GetMapping("/quote")
    public ResponseEntity<FareQuote> quote(@RequestParam("from") String fromCode, @RequestParam("to") String toCode) {
        logger.info("Request fare quote from: {} to: {}", fromCode, toCode);
        
        try {
            FareQuote quote = fareService.calculateFare(fromCode, toCode);
            
            if ("STATION_NOT_FOUND".equals(quote.mode)) {
                logger.warn("Station not found. From: {}, To: {}", fromCode, toCode);
                return ResponseEntity.badRequest().body(quote);
            }
            if ("NODES_NOT_FOUND".equals(quote.mode)) {
                logger.warn("Nodes not found in graph. From: {}, To: {}", fromCode, toCode);
                return ResponseEntity.badRequest().body(quote);
            }
            if ("UNREACHABLE".equals(quote.mode)) {
                 logger.warn("Path unreachable. From: {}, To: {}", fromCode, toCode);
                 // Unreachable is valid result, but maybe returning 400 is not ideal? 
                 // Frontend handles error msg from body, so 200 with error mode is also an option.
                 // But current logic uses 400 for errors.
                 return ResponseEntity.badRequest().body(quote);
            }
            
            return ResponseEntity.ok(quote);
        } catch (Exception e) {
            logger.error("Error calculating fare", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

