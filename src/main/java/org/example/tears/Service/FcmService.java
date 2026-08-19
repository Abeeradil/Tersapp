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

        List<UserDevice> devices =
                userDeviceRepository
                        .findByUserIdAndActiveTrue(user.getId());

        log.info(
                "FCM devices found: userId={}, count={}",
                user.getId(),
                devices.size()
        );

        for (UserDevice device : devices) {

            try {

                Message message =
                        Message.builder()

                                .setNotification(
                                        com.google.firebase.messaging.Notification
                                                .builder()
                                                .setTitle(
                                                        notification.getTitle()
                                                )
                                                .setBody(
                                                        notification.getBody()
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

                                .setToken(
                                        device.getFcmToken()
                                )

                                .build();

                log.info(
                        "Attempting FCM send: userId={}, deviceId={}",
                        user.getId(),
                        device.getId()
                );

                String messageId =
                        FirebaseMessaging
                                .getInstance()
                                .send(message);

                log.info(
                        "FCM sent successfully: userId={}, deviceId={}, messageId={}",
                        user.getId(),
                        device.getId(),
                        messageId
                );

            } catch (Exception e) {

                log.error(
                        "FCM send failed: userId={}, deviceId={}",
                        user.getId(),
                        device.getId(),
                        e
                );
            }
        }
    }
}