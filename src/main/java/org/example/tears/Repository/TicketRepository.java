package org.example.tears.Repository;

import org.example.tears.Enums.TicketStatus;
import org.example.tears.Model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByCustomer_IdOrderByCreatedAtDesc(Integer customerId);

    List<Ticket> findByAssignedEmployee_IdOrderByCreatedAtDesc(Integer employeeId);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    Optional<Ticket> findByRequest_IdAndStatusIn(
            Integer requestId,
            List<TicketStatus> statuses
    );

    List<Ticket> findByRequest_OrderNumberContainingIgnoreCase(String orderNumber);
}