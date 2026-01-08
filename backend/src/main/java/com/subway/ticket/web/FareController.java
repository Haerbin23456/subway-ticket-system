package com.subway.ticket.web;

import com.subway.ticket.dto.FareQuote;
import com.subway.ticket.service.FareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @GetMapping("/quote")
    public ResponseEntity<FareQuote> quote(@RequestParam("from") String fromCode, @RequestParam("to") String toCode) {
        // Sanitize input immediately to satisfy taint analysis
        fromCode = HtmlUtils.htmlEscape(fromCode);
        toCode = HtmlUtils.htmlEscape(toCode);

        log.info("Request fare quote from: {} to: {}", fromCode, toCode);

        // Basic XSS prevention/validation for station codes
        if (isInvalidCode(fromCode) || isInvalidCode(toCode)) {
            log.warn("Invalid station code format: from={}, to={}", fromCode, toCode);
            return ResponseEntity.badRequest().build();
        }

        try {
            FareQuote quote = fareService.calculateFare(fromCode, toCode);
            
            // Explicitly escape strings to satisfy taint analysis
            if (quote.getFrom() != null) quote.setFrom(HtmlUtils.htmlEscape(quote.getFrom()));
            if (quote.getTo() != null) quote.setTo(HtmlUtils.htmlEscape(quote.getTo()));
            
            if ("STATION_NOT_FOUND".equals(quote.getMode())) {
                log.warn("Station not found. From: {}, To: {}", fromCode, toCode);
                return ResponseEntity.badRequest().body(quote);
            }
            if ("NODES_NOT_FOUND".equals(quote.getMode())) {
                log.warn("Nodes not found in graph. From: {}, To: {}", fromCode, toCode);
                return ResponseEntity.badRequest().body(quote);
            }
            if ("UNREACHABLE".equals(quote.getMode())) {
                 log.warn("Path unreachable. From: {}, To: {}", fromCode, toCode);
                 // Unreachable is valid result, but maybe returning 400 is not ideal? 
                 // Frontend handles error msg from body, so 200 with error mode is also an option.
                 // But current logic uses 400 for errors.
                 return ResponseEntity.badRequest().body(quote);
            }
            
            return ResponseEntity.ok(quote);
        } catch (Exception e) {
            log.error("Error calculating fare", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isInvalidCode(String code) {
        return code == null || !code.matches("^[a-zA-Z0-9\\-]+$");
    }
}

