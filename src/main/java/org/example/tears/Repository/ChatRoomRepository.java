package org.example.tears.Repository;

import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.ChatRoom;
import org.example.tears.Model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    Optional<ChatRoom> findByTicket(Ticket ticket);
}