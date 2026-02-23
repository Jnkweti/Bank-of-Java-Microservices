package com.pm.analyticsservice.Controller;

import com.pm.analyticsservice.DTO.*;
import com.pm.analyticsservice.Service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private AnalyticsService analyticsService;

    @Test
    void getSummary_shouldReturn200WithSummaryBody() throws Exception {
        AnalyticsSummaryDTO dto = new AnalyticsSummaryDTO();
        dto.setTotalPayments(10L);
        dto.setCompletedPayments(8L);
        dto.setFailedPayments(2L);
        dto.setTotalVolume("5000.00");
        dto.setSuccessRate("80.0%");
        when(analyticsService.getSummary()).thenReturn(dto);

        mockMvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayments").value(10))
                .andExpect(jsonPath("$.completedPayments").value(8))
                .andExpect(jsonPath("$.totalVolume").value("5000.00"))
                .andExpect(jsonPath("$.successRate").value("80.0%"));
    }

    @Test
    void getDailyVolume_shouldReturn200WithList() throws Exception {
        List<DailyVolumeDTO> volume = List.of(
                new DailyVolumeDTO("2025-01-15", 3, "1500.00"),
                new DailyVolumeDTO("2025-01-16", 2, "800.50"));
        when(analyticsService.getDailyVolume(any(), any())).thenReturn(volume);

        mockMvc.perform(get("/api/analytics/volume")
                        .param("from", "2025-01-15")
                        .param("to", "2025-01-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2025-01-15"))
                .andExpect(jsonPath("$[0].count").value(3))
                .andExpect(jsonPath("$[1].volume").value("800.50"));
    }

    @Test
    void getDailyVolume_shouldReturn400_whenServiceThrowsIllegalArgument() throws Exception {
        when(analyticsService.getDailyVolume(any(), any()))
                .thenThrow(new IllegalArgumentException("'from' date must be before or equal to 'to' date"));

        mockMvc.perform(get("/api/analytics/volume")
                        .param("from", "2025-02-01")
                        .param("to", "2025-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void getTopAccounts_shouldReturn200WithLimit() throws Exception {
        List<TopAccountDTO> top = List.of(
                new TopAccountDTO("ACC-001", "15000.00"),
                new TopAccountDTO("ACC-002", "9500.00"));
        when(analyticsService.getTopAccounts(anyInt())).thenReturn(top);

        mockMvc.perform(get("/api/analytics/top-accounts").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value("ACC-001"))
                .andExpect(jsonPath("$[0].totalVolume").value("15000.00"))
                .andExpect(jsonPath("$[1].accountId").value("ACC-002"));
    }
}
