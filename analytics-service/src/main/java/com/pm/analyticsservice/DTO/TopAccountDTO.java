package com.pm.analyticsservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopAccountDTO {
    private String accountId;
    private String totalVolume;
}
