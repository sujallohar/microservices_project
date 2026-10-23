package com.govasset.finance.controller;

import com.govasset.finance.dto.FinanceDtos.*;
import com.govasset.finance.model.Budget;
import com.govasset.finance.repository.BudgetRepository;
import com.govasset.finance.service.ProcurementService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance")
public class FinanceController {

    private static final Logger log = LoggerFactory.getLogger(FinanceController.class);

    private final ProcurementService procurementService;
    private final BudgetRepository budgetRepository;

    public FinanceController(ProcurementService procurementService, BudgetRepository budgetRepository) {
        this.procurementService = procurementService;
        this.budgetRepository = budgetRepository;
    }

    @PostMapping("/procurement")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        try {
            PurchaseOrderResponse response = procurementService.createPurchaseOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Draft purchase order created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PostMapping("/procurement/{poId}/approve")
    @CircuitBreaker(name = "identityVerificationBreaker", fallbackMethod = "procurementFallback")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> approveProcurement(
            @PathVariable String poId,
            @RequestHeader(value = "X-Username", required = false, defaultValue = "FINANCE_DIRECTOR") String approvedBy) {
        try {
            PurchaseOrderResponse response = procurementService.approveProcurement(poId, approvedBy);
            return ResponseEntity.ok(ApiResponse.ok("Procurement approved; Saga initiated via Transactional Outbox", response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("Error approving procurement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("Error processing procurement approval: " + e.getMessage()));
        }
    }

    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> procurementFallback(String poId, String approvedBy, Throwable t) {
        log.warn("Circuit Breaker intercepted procurement approval for PO {}. Error: {}", poId, t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail("Finance Service Circuit Breaker is active: " + t.getMessage()));
    }

    @GetMapping("/procurement")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getAllPurchaseOrders() {
        return ResponseEntity.ok(ApiResponse.ok("List of purchase orders", procurementService.getAllPurchaseOrders()));
    }

    @GetMapping("/procurement/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrderById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Purchase order details", procurementService.getPurchaseOrderById(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        }
    }

    @GetMapping("/budgets")
    public ResponseEntity<ApiResponse<List<Budget>>> getBudgets() {
        return ResponseEntity.ok(ApiResponse.ok("Departmental budgets", budgetRepository.findAll()));
    }

    @PostMapping("/procurement/{poId}/compensate-test")
    public ResponseEntity<ApiResponse<String>> triggerCompensationTest(
            @PathVariable String poId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null && body.containsKey("reason") ? body.get("reason") : "Manual compensation simulation";
        SagaCompensationEvent event = new SagaCompensationEvent(
                "evt-manual-" + System.currentTimeMillis(),
                poId,
                "",
                "DIAGNOSTIC_TEST",
                reason,
                System.currentTimeMillis()
        );
        procurementService.compensateProcurement(event);
        return ResponseEntity.ok(ApiResponse.ok("Compensation rollback executed for PO: " + poId, null));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "finance-service", "database", "PostgreSQL"));
    }
}
