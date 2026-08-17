package org.example.tears.Repository;

import org.example.tears.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer>  {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    Optional<Notification> findByIdAndUserId(
            Integer id,
            Integer userId
    );

    }
