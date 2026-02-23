package com.pm.notificationservice.Mapper;

import com.pm.notificationservice.DTO.NotificationResponseDTO;
import com.pm.notificationservice.model.Notification;

public class NotificationMapper {

    public static NotificationResponseDTO toDTO(Notification n) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(n.getId().toString());
        dto.setPaymentId(n.getPaymentId());
        dto.setFromAccountId(n.getFromAccountId());
        dto.setToAccountId(n.getToAccountId());
        dto.setType(n.getType().name());
        dto.setMessage(n.getMessage());
        dto.setSentAt(n.getSentAt().toString());
        return dto;
    }
}
