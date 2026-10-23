package com.govasset.finance.service;

import com.govasset.finance.model.OutboxEvent;
import com.govasset.finance.model.OutboxEvent.OutboxStatus;
import com.govasset.finance.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OutboxRelayPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${govasset.rabbitmq.exchange:govasset.topic.exchange}")
    private String exchange;

    @Value("${govasset.rabbitmq.outbox.routing-key:finance.procurement.approved}")
    private String routingKey;

    public OutboxRelayPublisher(OutboxEventRepository outboxEventRepository, RabbitTemplate rabbitTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING, PageRequest.of(0, 20));

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Outbox Relay found {} pending events to dispatch to RabbitMQ", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                rabbitTemplate.convertAndSend(exchange, routingKey, event.getPayload(), message -> {
                    message.getMessageProperties().setContentType("application/json");
                    message.getMessageProperties().setHeader("eventType", event.getEventType());
                    message.getMessageProperties().setHeader("aggregateId", event.getAggregateId());
                    return message;
                });

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

                log.info("CDC Outbox Relay: Dispatched event [{}] to exchange [{}] with routingKey [{}]",
                        event.getEventType(), exchange, routingKey);

            } catch (Exception e) {
                log.error("Outbox Relay dispatch failure for event {}: {}", event.getId(), e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= 5) {
                    event.setStatus(OutboxStatus.FAILED);
                }
                outboxEventRepository.save(event);
            }
        }
    }
}
