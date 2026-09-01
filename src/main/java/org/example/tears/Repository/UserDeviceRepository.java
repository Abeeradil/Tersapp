package org.example.tears.Repository;

import org.example.tears.Model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Integer> {

    Optional<UserDevice> findByFcmToken(String fcmToken);

    Optional<UserDevice> findByFcmTokenAndUserId(String fcmToken, Integer userId);

    List<UserDevice> findByUserIdAndActiveTrue(Integer userId);

    List<UserDevice> findAllByOrderByUpdatedAtDesc();
}