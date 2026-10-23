package com.govasset.finance.dto;

import com.govasset.finance.model.PurchaseOrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FinanceDtos {

    public static class CreatePurchaseOrderRequest {
        @NotBlank(message = "Asset name is required")
        private String assetName;

        @NotBlank(message = "Asset category is required")
        private String assetCategory;

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "1.0", message = "Unit cost must be positive")
        private BigDecimal unitCost;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotBlank(message = "Department is required")
        private String department;

        @NotBlank(message = "Vendor name is required")
        private String vendorName;

        private String specifications;

        public CreatePurchaseOrderRequest() {}

        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }
        public String getAssetCategory() { return assetCategory; }
        public void setAssetCategory(String assetCategory) { this.assetCategory = assetCategory; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }
        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }
    }

    public static class PurchaseOrderResponse {
        private String id;
        private String poNumber;
        private String assetName;
        private String assetCategory;
        private BigDecimal unitCost;
        private Integer quantity;
        private BigDecimal totalCost;
        private String department;
        private String vendorName;
        private PurchaseOrderStatus status;
        private String approvedBy;
        private String specifications;
        private String compensationReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PurchaseOrderResponse() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPoNumber() { return poNumber; }
        public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }
        public String getAssetCategory() { return assetCategory; }
        public void setAssetCategory(String assetCategory) { this.assetCategory = assetCategory; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }
        public PurchaseOrderStatus getStatus() { return status; }
        public void setStatus(PurchaseOrderStatus status) { this.status = status; }
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }
        public String getCompensationReason() { return compensationReason; }
        public void setCompensationReason(String compensationReason) { this.compensationReason = compensationReason; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private PurchaseOrderResponse r = new PurchaseOrderResponse();
            public Builder id(String id) { r.id = id; return this; }
            public Builder poNumber(String num) { r.poNumber = num; return this; }
            public Builder assetName(String n) { r.assetName = n; return this; }
            public Builder assetCategory(String cat) { r.assetCategory = cat; return this; }
            public Builder unitCost(BigDecimal c) { r.unitCost = c; return this; }
            public Builder quantity(Integer q) { r.quantity = q; return this; }
            public Builder totalCost(BigDecimal t) { r.totalCost = t; return this; }
            public Builder department(String d) { r.department = d; return this; }
            public Builder vendorName(String v) { r.vendorName = v; return this; }
            public Builder status(PurchaseOrderStatus s) { r.status = s; return this; }
            public Builder approvedBy(String a) { r.approvedBy = a; return this; }
            public Builder specifications(String s) { r.specifications = s; return this; }
            public Builder compensationReason(String c) { r.compensationReason = c; return this; }
            public Builder createdAt(LocalDateTime c) { r.createdAt = c; return this; }
            public Builder updatedAt(LocalDateTime u) { r.updatedAt = u; return this; }
            public PurchaseOrderResponse build() { return r; }
        }
    }

    public static class ProcurementApprovedEvent {
        private String eventId;
        private String poId;
        private String poNumber;
        private String assetName;
        private String assetCategory;
        private BigDecimal unitCost;
        private Integer quantity;
        private BigDecimal totalCost;
        private String department;
        private String vendorName;
        private String specifications;
        private String approvedBy;
        private long timestamp;

        public ProcurementApprovedEvent() {}

        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        public String getPoId() { return poId; }
        public void setPoId(String poId) { this.poId = poId; }
        public String getPoNumber() { return poNumber; }
        public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }
        public String getAssetCategory() { return assetCategory; }
        public void setAssetCategory(String assetCategory) { this.assetCategory = assetCategory; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }
        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private ProcurementApprovedEvent e = new ProcurementApprovedEvent();
            public Builder eventId(String id) { e.eventId = id; return this; }
            public Builder poId(String id) { e.poId = id; return this; }
            public Builder poNumber(String num) { e.poNumber = num; return this; }
            public Builder assetName(String n) { e.assetName = n; return this; }
            public Builder assetCategory(String cat) { e.assetCategory = cat; return this; }
            public Builder unitCost(BigDecimal c) { e.unitCost = c; return this; }
            public Builder quantity(Integer q) { e.quantity = q; return this; }
            public Builder totalCost(BigDecimal t) { e.totalCost = t; return this; }
            public Builder department(String d) { e.department = d; return this; }
            public Builder vendorName(String v) { e.vendorName = v; return this; }
            public Builder specifications(String s) { e.specifications = s; return this; }
            public Builder approvedBy(String a) { e.approvedBy = a; return this; }
            public Builder timestamp(long ts) { e.timestamp = ts; return this; }
            public ProcurementApprovedEvent build() { return e; }
        }
    }

    public static class SagaCompensationEvent {
        private String eventId;
        private String poId;
        private String poNumber;
        private String failedService;
        private String reason;
        private long timestamp;

        public SagaCompensationEvent() {}

        public SagaCompensationEvent(String eventId, String poId, String poNumber, String failedService, String reason, long timestamp) {
            this.eventId = eventId;
            this.poId = poId;
            this.poNumber = poNumber;
            this.failedService = failedService;
            this.reason = reason;
            this.timestamp = timestamp;
        }

        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        public String getPoId() { return poId; }
        public void setPoId(String poId) { this.poId = poId; }
        public String getPoNumber() { return poNumber; }
        public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
        public String getFailedService() { return failedService; }
        public void setFailedService(String failedService) { this.failedService = failedService; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse() {}
        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> fail(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
}
