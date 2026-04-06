package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
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


    public void send(User user, String message){
        // تحقق من أن المستخدم فعّل الإشعارات
        if (user.getNotificationsEnabled() == null || !user.getNotificationsEnabled()) {
            // المستخدم لم يفعل الإشعارات، لا نفعل شيء
            return;
        }

        Notification n = new Notification();

        n.setUser(user);
        n.setMessage(message);
        n.setReadStatus(false);
        n.setCreatedAt(LocalDateTime.now());

        repo.save(n);
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



