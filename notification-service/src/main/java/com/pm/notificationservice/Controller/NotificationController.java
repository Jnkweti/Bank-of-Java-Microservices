package com.pm.notificationservice.Controller;

import com.pm.notificationservice.DTO.NotificationResponseDTO;
import com.pm.notificationservice.Service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Returns the notification audit log for a given account.
    // Includes both sent and received payment notifications.
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByAccount(
            @PathVariable String accountId) {
        return ResponseEntity.ok(notificationService.getNotificationsByAccount(accountId));
    }
}
