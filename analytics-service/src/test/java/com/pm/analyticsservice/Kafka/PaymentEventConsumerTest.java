package com.pm.analyticsservice.Kafka;

import com.pm.analyticsservice.DTO.PaymentEventDTO;
import com.pm.analyticsservice.Service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock    private AnalyticsService analyticsService;
    @InjectMocks private PaymentEventConsumer consumer;

    @Test
    void consume_shouldDelegateToAnalyticsService() {
        PaymentEventDTO event = new PaymentEventDTO();
        event.setPaymentId("PAY-001");
        event.setStatus("COMPLETED");
        event.setAmount("500.00");

        consumer.consume(event);

        verify(analyticsService).recordEvent(event);
    }
}
