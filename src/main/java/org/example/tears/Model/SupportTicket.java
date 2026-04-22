package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.tears.Enums.TicketPriority;
import org.example.tears.Enums.TicketStatus;
import org.example.tears.Enums.TicketType;

import java.time.LocalDateTime;

@Entity
@Data
@Setter
@Getter
public class SupportTicket {

        @Id
        @GeneratedValue
        private Integer id;

        private Integer customerId;

        private Integer assignedSupportId;

        @Enumerated
        private TicketStatus status;

        @Enumerated
        private TicketPriority priority;

        @Enumerated
        private TicketType type;

        private String subject;

        @Column(length=2000)
        private String description;

        private LocalDateTime createdAt;

}
