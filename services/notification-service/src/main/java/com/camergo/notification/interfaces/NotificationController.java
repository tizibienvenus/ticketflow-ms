package com.camergo.notification.interfaces;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boaz.ticketflow.common.dtos.NotificationRequest;
import com.boaz.ticketflow.common.wrappers.BaseResponse;
import com.camergo.notification.application.NotificationService;
import com.camergo.notification.application.dtos.NotificationRequestDto;
import com.camergo.notification.factory.NotificationRequestFactory;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationRequestFactory factory;

    @PostMapping("/send")
    public ResponseEntity<BaseResponse<String>> send(@RequestBody NotificationRequestDto dto) {
        NotificationRequest request = factory.create(dto);
        notificationService.sendNotification(request); // synchrone (ou asynchrone si souhaité)
        return ResponseEntity.accepted().body(BaseResponse.success("Notification accepted for processing"));
    }

    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse<?>> sendBulk(@RequestBody List<NotificationRequestDto> dtos) {
        
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(BaseResponse.error("Notification list cannot be empty"));
        }

        List<NotificationRequest> requests = dtos.stream()
            .map(factory::create)
            .toList();

        notificationService.sendBulkNotifications(requests);
        BaseResponse<Integer> response = BaseResponse.success(requests.size());

        return ResponseEntity.accepted().body(response);
    }
}