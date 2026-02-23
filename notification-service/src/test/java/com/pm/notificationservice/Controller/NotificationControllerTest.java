package com.pm.notificationservice.Controller;

import com.pm.notificationservice.DTO.NotificationResponseDTO;
import com.pm.notificationservice.Service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private NotificationService notificationService;

    private NotificationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new NotificationResponseDTO();
        responseDTO.setId("notif-id-001");
        responseDTO.setPaymentId("pay-001");
        responseDTO.setFromAccountId("acc-from");
        responseDTO.setToAccountId("acc-to");
        responseDTO.setType("PAYMENT_SUCCESS");
        responseDTO.setMessage("Your payment was successful.");
        responseDTO.setSentAt("2025-06-01T10:00:00");
    }

    @Test
    void getNotificationsByAccount_shouldReturn200WithList() throws Exception {
        when(notificationService.getNotificationsByAccount("acc-from"))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/notifications/account/{accountId}", "acc-from"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value("pay-001"))
                .andExpect(jsonPath("$[0].type").value("PAYMENT_SUCCESS"))
                .andExpect(jsonPath("$[0].message").value("Your payment was successful."));
    }

    @Test
    void getNotificationsByAccount_shouldReturn200WithEmptyList() throws Exception {
        when(notificationService.getNotificationsByAccount("acc-none"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/account/{accountId}", "acc-none"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
