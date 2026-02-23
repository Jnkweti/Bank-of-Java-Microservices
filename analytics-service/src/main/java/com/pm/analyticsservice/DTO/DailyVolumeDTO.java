package com.pm.analyticsservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyVolumeDTO {
    private String date;
    private long count;
    private String volume;
}
