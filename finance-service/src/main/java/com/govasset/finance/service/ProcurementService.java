package com.govasset.finance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govasset.finance.dto.FinanceDtos.*;
import com.govasset.finance.model.Budget;
import com.govasset.finance.model.OutboxEvent;
import com.govasset.finance.model.OutboxEvent.OutboxStatus;
import com.govasset.finance.model.PurchaseOrder;
import com.govasset.finance.model.PurchaseOrderStatus;
import com.govasset.finance.repository.BudgetRepository;
import com.govasset.finance.repository.OutboxEventRepository;
import com.govasset.finance.repository.PurchaseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProcurementService {

    private static final Logger log = LoggerFactory.getLogger(ProcurementService.class);

    private final PurchaseOrderRepository poRepository;
    private final BudgetRepository budgetRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ProcurementService(PurchaseOrderRepository poRepository, BudgetRepository budgetRepository,
                              OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.poRepository = poRepository;
        this.budgetRepository = budgetRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest req) {
        BigDecimal totalCost = req.getUnitCost().multiply(BigDecimal.valueOf(req.getQuantity()));
        String poNumber = "PO-GUJ-" + System.currentTimeMillis();

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(poNumber)
                .assetName(req.getAssetName())
                .assetCategory(req.getAssetCategory().toUpperCase())
                .unitCost(req.getUnitCost())
                .quantity(req.getQuantity())
                .totalCost(totalCost)
                .department(req.getDepartment())
                .vendorName(req.getVendorName())
                .specifications(req.getSpecifications())
                .status(PurchaseOrderStatus.DRAFT)
                .build();

        po = poRepository.save(po);
        log.info("Created draft purchase order: {} for department: {}", po.getPoNumber(), po.getDepartment());
        return mapToResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse approveProcurement(String poId, String approvedBy) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found: " + poId));

        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("PO is not in DRAFT state. Current state: " + po.getStatus());
        }

        final String department = po.getDepartment();
        Budget budget = budgetRepository.findByDepartmentAndFiscalYear(department, "2026-2027")
                .orElseGet(() -> {
                    Budget newBudget = Budget.builder()
                            .department(department)
                            .fiscalYear("2026-2027")
                            .allocatedAmount(new BigDecimal("50000000.00"))
                            .reservedAmount(BigDecimal.ZERO)
                            .disbursedAmount(BigDecimal.ZERO)
                            .build();
                    return budgetRepository.save(newBudget);
                });

        if (budget.getAvailableAmount().compareTo(po.getTotalCost()) < 0) {
            throw new IllegalStateException(String.format("Insufficient budget for department %s. Available: %s, Required: %s",
                    po.getDepartment(), budget.getAvailableAmount(), po.getTotalCost()));
        }

        budget.setReservedAmount(budget.getReservedAmount().add(po.getTotalCost()));
        budgetRepository.save(budget);

        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setApprovedBy(approvedBy != null ? approvedBy : "DIRECTOR_FINANCE");
        po = poRepository.save(po);

        String eventId = UUID.randomUUID().toString();
        ProcurementApprovedEvent eventPayload = ProcurementApprovedEvent.builder()
                .eventId(eventId)
                .poId(po.getId())
                .poNumber(po.getPoNumber())
                .assetName(po.getAssetName())
                .assetCategory(po.getAssetCategory())
                .unitCost(po.getUnitCost())
                .quantity(po.getQuantity())
                .totalCost(po.getTotalCost())
                .department(po.getDepartment())
                .vendorName(po.getVendorName())
                .specifications(po.getSpecifications())
                .approvedBy(po.getApprovedBy())
                .timestamp(System.currentTimeMillis())
                .build();

        try {
            String payloadJson = objectMapper.writeValueAsString(eventPayload);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("PURCHASE_ORDER")
                    .aggregateId(po.getId())
                    .eventType("ProcurementApprovedEvent")
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("ACID transaction complete: PO {} approved and event {} queued in Outbox",
                    po.getPoNumber(), eventId);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Outbox event payload", e);
        }

        return mapToResponse(po);
    }

    @Transactional
    public void compensateProcurement(SagaCompensationEvent event) {
        log.warn("Executing Saga Compensation for PO: {}. Reason: {}", event.getPoNumber(), event.getReason());

        PurchaseOrder po = poRepository.findById(event.getPoId())
                .or(() -> poRepository.findByPoNumber(event.getPoNumber()))
                .orElse(null);

        if (po == null) {
            log.error("Cannot compensate: PO {} not found", event.getPoNumber());
            return;
        }

        if (po.getStatus() == PurchaseOrderStatus.REJECTED_ROLLBACK) {
            log.info("PO {} already rolled back", po.getPoNumber());
            return;
        }

        budgetRepository.findByDepartmentAndFiscalYear(po.getDepartment(), "2026-2027")
                .ifPresent(budget -> {
                    BigDecimal updatedReserved = budget.getReservedAmount().subtract(po.getTotalCost());
                    if (updatedReserved.compareTo(BigDecimal.ZERO) < 0) {
                        updatedReserved = BigDecimal.ZERO;
                    }
                    budget.setReservedAmount(updatedReserved);
                    budgetRepository.save(budget);
                    log.info("Released reserved budget for department {}: restored {}", po.getDepartment(), po.getTotalCost());
                });

        po.setStatus(PurchaseOrderStatus.REJECTED_ROLLBACK);
        po.setCompensationReason(String.format("Rolled back by Saga. Failed Service: [%s]. Reason: [%s]",
                event.getFailedService(), event.getReason()));
        poRepository.save(po);

        log.info("Saga compensation successfully applied to PO: {}", po.getPoNumber());
    }

    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return poRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public PurchaseOrderResponse getPurchaseOrderById(String id) {
        return poRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("PO not found: " + id));
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrder po) {
        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .poNumber(po.getPoNumber())
                .assetName(po.getAssetName())
                .assetCategory(po.getAssetCategory())
                .unitCost(po.getUnitCost())
                .quantity(po.getQuantity())
                .totalCost(po.getTotalCost())
                .department(po.getDepartment())
                .vendorName(po.getVendorName())
                .status(po.getStatus())
                .approvedBy(po.getApprovedBy())
                .specifications(po.getSpecifications())
                .compensationReason(po.getCompensationReason())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }
}
