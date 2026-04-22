package org.example.tears.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
@Setter
@Getter
public class TicketMessage {

        @Id
        @GeneratedValue
        private Integer id;

        private Integer ticketId;

        private Integer senderId;

        private String message;

        private String attachmentUrl;

        private LocalDateTime sentAt;

}
