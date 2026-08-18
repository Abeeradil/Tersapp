package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Model.Notification;
import org.example.tears.Model.User;
import org.example.tears.Model.UserDevice;
import org.example.tears.Repository.UserDeviceRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;

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

                FirebaseMessaging
                        .getInstance()
                        .send(message);

            } catch (Exception e) {

                // لاحقاً نسجل الخطأ
                // ونقدر نعطل الـ token غير الصالح
            }
        }
    }


}