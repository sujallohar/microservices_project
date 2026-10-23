package com.govasset.finance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String poNumber;

    @Column(nullable = false, length = 255)
    private String assetName;

    @Column(nullable = false, length = 64)
    private String assetCategory;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitCost;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCost;

    @Column(nullable = false, length = 64)
    private String department;

    @Column(nullable = false, length = 255)
    private String vendorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PurchaseOrderStatus status;

    private String approvedBy;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    @Column(columnDefinition = "TEXT")
    private String compensationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PurchaseOrder() {}

    public PurchaseOrder(String id, String poNumber, String assetName, String assetCategory, BigDecimal unitCost,
                         Integer quantity, BigDecimal totalCost, String department, String vendorName,
                         PurchaseOrderStatus status, String approvedBy, String specifications,
                         String compensationReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.poNumber = poNumber;
        this.assetName = assetName;
        this.assetCategory = assetCategory;
        this.unitCost = unitCost;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.department = department;
        this.vendorName = vendorName;
        this.status = status;
        this.approvedBy = approvedBy;
        this.specifications = specifications;
        this.compensationReason = compensationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PurchaseOrderStatus.DRAFT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private PurchaseOrder po = new PurchaseOrder();
        public Builder id(String id) { po.id = id; return this; }
        public Builder poNumber(String num) { po.poNumber = num; return this; }
        public Builder assetName(String name) { po.assetName = name; return this; }
        public Builder assetCategory(String cat) { po.assetCategory = cat; return this; }
        public Builder unitCost(BigDecimal cost) { po.unitCost = cost; return this; }
        public Builder quantity(Integer q) { po.quantity = q; return this; }
        public Builder totalCost(BigDecimal tot) { po.totalCost = tot; return this; }
        public Builder department(String d) { po.department = d; return this; }
        public Builder vendorName(String v) { po.vendorName = v; return this; }
        public Builder status(PurchaseOrderStatus s) { po.status = s; return this; }
        public Builder approvedBy(String a) { po.approvedBy = a; return this; }
        public Builder specifications(String s) { po.specifications = s; return this; }
        public Builder compensationReason(String c) { po.compensationReason = c; return this; }
        public PurchaseOrder build() { return po; }
    }
}
