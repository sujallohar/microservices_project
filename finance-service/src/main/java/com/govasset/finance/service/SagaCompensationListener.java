package com.govasset.finance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govasset.finance.dto.FinanceDtos.SagaCompensationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SagaCompensationListener {

    private static final Logger log = LoggerFactory.getLogger(SagaCompensationListener.class);

    private final ProcurementService procurementService;
    private final ObjectMapper objectMapper;

    public SagaCompensationListener(ProcurementService procurementService, ObjectMapper objectMapper) {
        this.procurementService = procurementService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${govasset.rabbitmq.compensation-queue:queue.finance.compensations}")
    public void handleSagaCompensation(org.springframework.amqp.core.Message message) {
        try {
            String messagePayload = new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8);
            log.warn("Saga Compensation Listener received compensation trigger: {}", messagePayload);
            SagaCompensationEvent event = objectMapper.readValue(messagePayload, SagaCompensationEvent.class);
            procurementService.compensateProcurement(event);
        } catch (Exception e) {
            log.error("Failed to process Saga compensation message: {}", e.getMessage(), e);
        }
    }
}
