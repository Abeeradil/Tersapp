package org.example.tears.Repository;

import org.example.tears.Enums.ReadStatus;
import org.example.tears.Model.ChatMessage;
import org.example.tears.Model.MessageStatus;
import org.example.tears.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, Integer> {

    Optional<MessageStatus> findByMessageAndUser(
            ChatMessage message,
            User user
    );

    List<MessageStatus> findByUserAndStatus(
            User user,
            ReadStatus status
    );
}
