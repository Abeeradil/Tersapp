package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Model.Notification;
import org.example.tears.Model.User;
import org.example.tears.Service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tears/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    // إشعاراتي
    @GetMapping("/my-notification")
    public List<Notification> myNotifications(
            @AuthenticationPrincipal User user
    ) {
        return notificationService
                .getUserNotifications(user.getId());
    }


    // قراءة إشعار
    @PutMapping("notification/{id}/read")
    public ApiResponse markRead(@PathVariable Integer id) {

        notificationService.markAsRead(id);

        return new ApiResponse(true,"تم التحديث");
    }
}
