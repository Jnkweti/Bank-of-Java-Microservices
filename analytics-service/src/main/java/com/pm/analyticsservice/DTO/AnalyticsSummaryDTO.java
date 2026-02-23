package com.pm.analyticsservice.DTO;

import lombok.Data;

@Data
public class AnalyticsSummaryDTO {
    private long totalPayments;
    private long completedPayments;
    private long failedPayments;
    private String totalVolume;
    private String successRate;
}
