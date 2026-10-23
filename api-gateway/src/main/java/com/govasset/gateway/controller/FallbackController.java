package com.govasset.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "success", false,
                "error", "Identity & RBAC Service is currently unavailable or timing out. Circuit breaker is OPEN.",
                "service", "identity-service",
                "status", 503
        )));
    }

    @RequestMapping("/finance")
    public Mono<ResponseEntity<Map<String, Object>>> financeFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "success", false,
                "error", "Finance & Procurement Service is currently unavailable. Circuit breaker has intercepted the request.",
                "service", "finance-service",
                "status", 503
        )));
    }

    @RequestMapping("/assets")
    public Mono<ResponseEntity<Map<String, Object>>> assetsFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "success", false,
                "error", "Core Asset Registry is currently unavailable. Request protected by Circuit Breaker.",
                "service", "asset-registry-service",
                "status", 503
        )));
    }

    @RequestMapping("/maintenance")
    public Mono<ResponseEntity<Map<String, Object>>> maintenanceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "success", false,
                "error", "Maintenance & Lifecycle Service is currently experiencing high latency or downtime.",
                "service", "maintenance-service",
                "status", 503
        )));
    }
}
