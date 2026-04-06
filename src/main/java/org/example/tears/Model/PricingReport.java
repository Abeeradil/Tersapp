package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
@Setter
@Getter
public class PricingReport {

        @Id
        @GeneratedValue
        private Integer id;

        @OneToOne
        private CarServiceRequest request;

        private Integer totalParts;
        private Integer totalLabor;
        private Integer totalPrice;

        private LocalDateTime sentAt;
}
