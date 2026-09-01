package org.example.tears.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tears.Model.Notification;
import org.example.tears.Model.User;
import org.example.tears.Model.UserDevice;
import org.example.tears.Repository.UserDeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final UserDeviceRepository userDeviceRepository;

    public void send(
            User user,
            Notification notification
    ) {

        List<String> tokens =
                userDeviceRepository
                        .findByUserIdAndActiveTrue(user.getId())
                        .stream()
                        .map(UserDevice::getFcmToken)
                        .filter(token -> token != null && !token.isBlank())
                        .distinct()
                        .toList();

        log.info(
                "FCM devices found: userId={}, count={}",
                user.getId(),
                tokens.size()
        );

        for (String token : tokens) {

            try {

                Message message =
                        Message.builder()

                                .setNotification(
                                        com.google.firebase.messaging.Notification
                                                .builder()
                                                .setTitle(notification.getTitle())
                                                .setBody(notification.getBody())
                                                .build()
                                )

                                .setAndroidConfig(
                                        com.google.firebase.messaging.AndroidConfig.builder()
                                                .setNotification(
                                                        com.google.firebase.messaging.AndroidNotification.builder()
                                                                .setChannelId("ters_employee_alerts_v2")
                                                                .setSound("mixkit_keys_moving")
                                                                .build()
                                                )
                                                .build()
                                )

                                .setApnsConfig(
                                        com.google.firebase.messaging.ApnsConfig.builder()
                                                .setAps(
                                                        com.google.firebase.messaging.Aps.builder()
                                                                .setSound("mixkit-keys-moving.wav")
                                                                .build()
                                                )
                                                .build()
                                )

                                .putData(
                                        "notificationId",
                                        notification.getId().toString()
                                )

                                .putData(
                                        "type",
                                        notification.getType().name()
                                )

                                .putData(
                                        "category",
                                        notification.getCategory().name()
                                )

                                .putData(
                                        "actionType",
                                        notification.getActionType().name()
                                )

                                .putData(
                                        "entityType",
                                        notification.getEntityType().name()
                                )

                                .putData(
                                        "entityId",
                                        notification.getEntityId()
                                )

                                .putData(
                                        "section",
                                        notification.getSection().name()
                                )

                                .setToken(token)

                                .build();

                log.info(
                        "Attempting FCM send: userId={}",
                        user.getId()
                );

                String messageId =
                        FirebaseMessaging
                                .getInstance()
                                .send(message);

                log.info(
                        "FCM sent successfully: userId={}, messageId={}",
                        user.getId(),
                        messageId
                );

            } catch (Exception e) {

                log.error(
                        "FCM send failed: userId={}",
                        user.getId(),
                        e
                );
            }
        }
    }
}