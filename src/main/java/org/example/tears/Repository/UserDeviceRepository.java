package org.example.tears.Repository;

import org.example.tears.Model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository
        extends JpaRepository<UserDevice, Integer> {

    List<UserDevice> findByUserIdAndActiveTrue(Integer userId);

    Optional<UserDevice> findByFcmToken(String fcmToken);

    Optional<UserDevice> findByFcmTokenAndUserId(
            String fcmToken,
            Integer userId
    );
}