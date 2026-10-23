package com.govasset.finance.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status", columnList = "status"),
        @Index(name = "idx_outbox_created", columnList = "createdAt")
})
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 64)
    private String aggregateType;

    @Column(nullable = false, length = 64)
    private String aggregateId;

    @Column(nullable = false, length = 128)
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OutboxStatus status;

    private Integer retryCount;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public enum OutboxStatus {
        PENDING,
        PUBLISHED,
        FAILED
    }

    public OutboxEvent() {}

    public OutboxEvent(String id, String aggregateType, String aggregateId, String eventType, String payload,
                       OutboxStatus status, Integer retryCount, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public OutboxStatus getStatus() { return status; }
    public void setStatus(OutboxStatus status) { this.status = status; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = OutboxStatus.PENDING;
        if (this.retryCount == null) this.retryCount = 0;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private OutboxEvent evt = new OutboxEvent();
        public Builder id(String id) { evt.id = id; return this; }
        public Builder aggregateType(String type) { evt.aggregateType = type; return this; }
        public Builder aggregateId(String aggId) { evt.aggregateId = aggId; return this; }
        public Builder eventType(String type) { evt.eventType = type; return this; }
        public Builder payload(String p) { evt.payload = p; return this; }
        public Builder status(OutboxStatus s) { evt.status = s; return this; }
        public Builder retryCount(Integer r) { evt.retryCount = r; return this; }
        public Builder createdAt(LocalDateTime c) { evt.createdAt = c; return this; }
        public Builder processedAt(LocalDateTime pr) { evt.processedAt = pr; return this; }
        public OutboxEvent build() { return evt; }
    }
}
