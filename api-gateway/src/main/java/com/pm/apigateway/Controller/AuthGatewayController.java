package com.pm.apigateway.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthGatewayController {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authUrl;

    public AuthGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Map<String, String> body) {
        return restTemplate.postForEntity(authUrl + "/api/auth/register", body, Object.class);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {
        return restTemplate.postForEntity(authUrl + "/api/auth/login", body, Object.class);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestBody Map<String, String> body) {
        return restTemplate.postForEntity(authUrl + "/api/auth/refresh", body, Object.class);
    }
}
