package com.example.EDPaySystem.consumer;

import com.example.EDPaySystem.config.RabbitMQProperties;
import com.example.EDPaySystem.dto.PaymentEvent;
import com.example.EDPaySystem.service.IdempotencyService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpTemplate;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseConsumer {

    @Autowired
    protected AmqpTemplate amqpTemplate;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private MeterRegistry meterRegistry;

    protected boolean isDuplicate(PaymentEvent event) {
        if(idempotencyService.isAlreadyProcessed(event.getPaymentId())) {
            System.out.println("[IDEMPOTENCY] Duplicate detected, skipping: "
                    + event.getPaymentId());
            return true;
        }
        return false;
    }

    protected void markProcessed(PaymentEvent event) {
        idempotencyService.markAsProcessed(event.
                getPaymentId());
    }

    protected void handleRetry(PaymentEvent event, String retryRoutingKey, String dlqRoutingKey, Exception e) {


        String channel = dlqRoutingKey.split("\\.")[0].toUpperCase();
        meterRegistry.counter("payment_retry_total", "channel",  channel).increment();
        int currentRetryCount = event.getRetryCount();

        System.err.println("[" + channel + "] Failed (attempt " + (currentRetryCount + 1) + "): " + e.getMessage());

        if(currentRetryCount < RabbitMQProperties.MAX_RETRY_COUNT) {
            event.setRetryCount(currentRetryCount + 1);
            System.err.println("[" + channel + "] Scheduling retry "
                    + event.getRetryCount() + " in "
                    + RabbitMQProperties.RETRY_DELAY_MS + "ms");

            // Publish to retry queue via DLX
            amqpTemplate.convertAndSend(
                    RabbitMQProperties.DLX_NAME,
                    retryRoutingKey,
                    event
            );
        } else {
            System.err.println("[" + channel + "] Max retries exceeded. Routing to DLQ.");
            amqpTemplate.convertAndSend(
                    RabbitMQProperties.DLX_NAME,
                    dlqRoutingKey,
                    event
            );
        }
        throw new AmqpRejectAndDontRequeueException("Handled by retry logic", false, null); // reduces verbosity
    }

}
