package com.pm.analyticsservice.Controller;

import com.pm.analyticsservice.DTO.AnalyticsSummaryDTO;
import com.pm.analyticsservice.DTO.DailyVolumeDTO;
import com.pm.analyticsservice.DTO.TopAccountDTO;
import com.pm.analyticsservice.Service.AnalyticsService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@AllArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDTO> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }

    // @DateTimeFormat tells Spring how to parse the query param string into LocalDate.
    // Without it, Spring would not know the format and throw a conversion error.
    @GetMapping("/volume")
    public ResponseEntity<List<DailyVolumeDTO>> getDailyVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getDailyVolume(from, to));
    }

    @GetMapping("/top-accounts")
    public ResponseEntity<List<TopAccountDTO>> getTopAccounts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopAccounts(limit));
    }
}
