package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.NotificationListResponse;
import org.example.tears.DTO.RegisterDeviceDto;
import org.example.tears.Model.User;
import org.example.tears.Service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tears/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    // إشعاراتي
    @GetMapping("/my-notification")
    public NotificationListResponse myNotifications(
            @AuthenticationPrincipal User user
    ) {
        return notificationService
                .getUserNotifications(user.getId());
    }


    // قراءة إشعار
    @PutMapping("/notification/{id}/read")
    public ApiResponse markRead(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {

        notificationService.markAsRead(
                id,
                user.getId()
        );

        return new ApiResponse(true, "تم تحديث الإشعار");
    }

    @PutMapping("/read-all")
    public ApiResponse markAllAsRead(
            @AuthenticationPrincipal User user
    ) {

        notificationService.markAllAsRead(user.getId());

        return new ApiResponse(
                true,
                "تم تحديث جميع الإشعارات"
        );
    }
    @PostMapping("/device")
    public ApiResponse registerDevice(
            @AuthenticationPrincipal User user,
            @RequestBody RegisterDeviceDto dto
    ) {

        notificationService.registerDevice(
                user,
                dto
        );

        return new ApiResponse(
                true,
                "تم تسجيل الجهاز"
        );
    }
}
