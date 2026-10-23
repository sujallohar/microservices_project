package com.govasset.finance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 64)
    private String department;

    @Column(nullable = false, length = 16)
    private String fiscalYear;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal reservedAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal disbursedAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Budget() {}

    public Budget(String id, String department, String fiscalYear, BigDecimal allocatedAmount,
                  BigDecimal reservedAmount, BigDecimal disbursedAmount, LocalDateTime createdAt,
                  LocalDateTime updatedAt) {
        this.id = id;
        this.department = department;
        this.fiscalYear = fiscalYear;
        this.allocatedAmount = allocatedAmount;
        this.reservedAmount = reservedAmount;
        this.disbursedAmount = disbursedAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    public BigDecimal getReservedAmount() { return reservedAmount; }
    public void setReservedAmount(BigDecimal reservedAmount) { this.reservedAmount = reservedAmount; }
    public BigDecimal getDisbursedAmount() { return disbursedAmount; }
    public void setDisbursedAmount(BigDecimal disbursedAmount) { this.disbursedAmount = disbursedAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.reservedAmount == null) this.reservedAmount = BigDecimal.ZERO;
        if (this.disbursedAmount == null) this.disbursedAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getAvailableAmount() {
        return allocatedAmount.subtract(reservedAmount).subtract(disbursedAmount);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Budget b = new Budget();
        public Builder id(String id) { b.id = id; return this; }
        public Builder department(String d) { b.department = d; return this; }
        public Builder fiscalYear(String fy) { b.fiscalYear = fy; return this; }
        public Builder allocatedAmount(BigDecimal amt) { b.allocatedAmount = amt; return this; }
        public Builder reservedAmount(BigDecimal amt) { b.reservedAmount = amt; return this; }
        public Builder disbursedAmount(BigDecimal amt) { b.disbursedAmount = amt; return this; }
        public Budget build() { return b; }
    }
}
