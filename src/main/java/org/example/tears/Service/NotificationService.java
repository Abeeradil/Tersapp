package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.NotificationDto;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Notification;
import org.example.tears.Model.User;
import org.example.tears.Repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

        private final NotificationRepository repo;
    private final SocketService socketService;

    public void send(User user, String message) {

        Notification n = new Notification();

        n.setUser(user);
        n.setMessage(message);
        n.setReadStatus(false);
        n.setCreatedAt(LocalDateTime.now());

        Notification saved =
                repo.save(n);

        if (user.getNotificationsEnabled() != null &&
                user.getNotificationsEnabled()) {

            socketService.send(
                    "/topic/notifications/" + user.getId(),
                    toDto(saved)
            );
        }
    }

    private NotificationDto toDto(Notification notification) {

        NotificationDto dto = new NotificationDto();

        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setReadStatus(notification.isReadStatus());
        dto.setCreatedAt(notification.getCreatedAt());

        return dto;
    }

        // -----------------------------
        // جلب إشعارات المستخدم
        // -----------------------------
        public List<Notification> getUserNotifications(Integer userId) {
            return repo.findByUserIdOrderByCreatedAtDesc(userId);
        }

        // -----------------------------
        // تعليم إشعار كمقروء
        // -----------------------------
        public void markAsRead(Integer notificationId) {
            Notification notification = repo.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));
            notification.setReadStatus(true);
            repo.save(notification);
        }
    }



