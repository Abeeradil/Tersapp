package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.NotificationActionDto;
import org.example.tears.DTO.NotificationDto;
import org.example.tears.DTO.NotificationListResponse;
import org.example.tears.DTO.RegisterDeviceDto;
import org.example.tears.Model.Notification;
import org.example.tears.Model.User;
import org.example.tears.Model.UserDevice;
import org.example.tears.Repository.NotificationRepository;
import org.example.tears.Enums.NotificationActionType;
import org.example.tears.Enums.NotificationCategory;
import org.example.tears.Enums.NotificationEntityType;
import org.example.tears.Enums.NotificationSection;
import org.example.tears.Enums.NotificationType;
import org.example.tears.Repository.UserDeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final SocketService socketService;
    private final FcmService fcmService;
    private final UserDeviceRepository userDeviceRepository;



    public void send(
            User user,
            NotificationType type,
            NotificationCategory category,
            String title,
            String body,
            NotificationActionType actionType,
            NotificationEntityType entityType,
            String entityId,
            NotificationSection section
    ) {

        if (user == null) {
            return;
        }

        Notification n = new Notification();

        n.setUser(user);

        n.setType(type);
        n.setCategory(category);
        n.setTitle(title);
        n.setBody(body);

        n.setActionType(actionType);
        n.setEntityType(entityType);
        n.setEntityId(entityId);
        n.setSection(section);

        n.setReadStatus(false);
        n.setReadAt(null);
        n.setCreatedAt(LocalDateTime.now());

        Notification saved = repo.save(n);

        log.info(
                "Notification saved: userId={}, notificationId={}",
                user.getId(),
                saved.getId()
        );

// WebSocket + FCM
        if (Boolean.TRUE.equals(user.getNotificationsEnabled())) {

            socketService.send(
                    "/topic/notifications/" + user.getId(),
                    toDto(saved)
            );

            log.info(
                    "Sending FCM notification: userId={}, notificationId={}",
                    user.getId(),
                    saved.getId()
            );

            fcmService.send(user, saved);
        }
    }

    private NotificationDto toDto(Notification notification) {

        NotificationActionDto action = new NotificationActionDto();

        action.setType(notification.getActionType());
        action.setEntityType(notification.getEntityType());
        action.setEntityId(notification.getEntityId());
        action.setSection(notification.getSection());

        NotificationDto dto = new NotificationDto();

        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setCategory(notification.getCategory());
        dto.setTitle(notification.getTitle());
        dto.setBody(notification.getBody());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadStatus(notification.isReadStatus());
        dto.setReadAt(notification.getReadAt());
        dto.setAction(action);

        return dto;
    }

    public NotificationListResponse getUserNotifications(
            Integer userId
    ) {

        List<Notification> notifications =
                repo.findByUserIdOrderByCreatedAtDesc(userId);

        List<NotificationDto> items =
                notifications.stream()
                        .map(this::toDto)
                        .toList();

        long unreadCount =
                notifications.stream()
                        .filter(notification ->
                                !notification.isReadStatus())
                        .count();

        return new NotificationListResponse(
                items,
                unreadCount,
                false,
                null
        );
    }

    public void markAsRead(
            Integer notificationId,
            Integer userId
    ) {

        Notification notification =
                repo.findByIdAndUserId(
                        notificationId,
                        userId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Notification not found"
                        )
                );

        notification.setReadStatus(true);
        notification.setReadAt(LocalDateTime.now());

        repo.save(notification);
    }

    @Transactional
    public void markAllAsRead(Integer userId) {

        List<Notification> notifications =
                repo.findByUserIdOrderByCreatedAtDesc(userId);

        LocalDateTime now = LocalDateTime.now();

        notifications.stream()
                .filter(notification -> !notification.isReadStatus())
                .forEach(notification -> {
                    notification.setReadStatus(true);
                    notification.setReadAt(now);
                });

        repo.saveAll(notifications);
    }

    public void registerDevice(
            User user,
            RegisterDeviceDto dto
    ) {

        UserDevice device =
                userDeviceRepository
                        .findByFcmToken(dto.getFcmToken())
                        .orElse(new UserDevice());

        device.setUser(user);
        device.setFcmToken(dto.getFcmToken());
        device.setDeviceType(dto.getDeviceType());
        device.setActive(true);
        device.setLastSeen(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());

        if (device.getCreatedAt() == null) {
            device.setCreatedAt(LocalDateTime.now());
        }

        userDeviceRepository.save(device);
    }

    @Transactional
    public void unregisterDevice(User user, String fcmToken) {

        if (fcmToken == null || fcmToken.isBlank()) {
            throw new ApiException("FCM token is required");
        }

        UserDevice device =
                userDeviceRepository
                        .findByFcmTokenAndUserId(
                                fcmToken,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ApiException("Device not found"));

        device.setActive(false);
        device.setUpdatedAt(LocalDateTime.now());

        userDeviceRepository.save(device);
    }
}