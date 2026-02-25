package com.pm.apigateway.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsGatewayController {

    private final RestTemplate restTemplate;

    // base URL comes from application.yml — makes it easy to swap per environment
    @Value("${analytics.service.url}")
    private String analyticsUrl;

    public AnalyticsGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/summary")
    public ResponseEntity<Object> getSummary() {
        return restTemplate.getForEntity(analyticsUrl + "/api/analytics/summary", Object.class);
    }

    @GetMapping("/volume")
    public ResponseEntity<Object> getDailyVolume(@RequestParam String from,
                                                  @RequestParam String to) {
        String url = analyticsUrl + "/api/analytics/volume?from=" + from + "&to=" + to;
        return restTemplate.getForEntity(url, Object.class);
    }

    @GetMapping("/top-accounts")
    public ResponseEntity<Object> getTopAccounts(@RequestParam(defaultValue = "10") int limit) {
        String url = analyticsUrl + "/api/analytics/top-accounts?limit=" + limit;
        return restTemplate.getForEntity(url, Object.class);
    }
}
