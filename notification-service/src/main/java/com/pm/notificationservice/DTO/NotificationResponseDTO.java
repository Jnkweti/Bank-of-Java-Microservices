package com.pm.notificationservice.DTO;

import lombok.Data;

@Data
public class NotificationResponseDTO {
    private String id;
    private String paymentId;
    private String fromAccountId;
    private String toAccountId;
    private String type;
    private String message;
    private String sentAt;
}
