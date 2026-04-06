package org.example.tears.Repository;

import jdk.jfr.Registered;
import org.example.tears.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer>  {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);


    }
