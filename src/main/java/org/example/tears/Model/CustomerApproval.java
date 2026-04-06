package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
@Entity
@Data
@Setter
@Getter
public class CustomerApproval {

        @Id
        @GeneratedValue
        private Integer id;

        @OneToOne
        private CarServiceRequest request;

        private boolean approved;

        @Column(length = 1000)
        private String rejectedParts;

        private LocalDateTime responseAt;

}
