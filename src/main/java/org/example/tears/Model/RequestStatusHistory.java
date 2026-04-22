package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.StaffRequestStatus;

import java.time.LocalDateTime;

@Entity
@Data
@Setter
@Getter
public class RequestStatusHistory {

                @Id
                @GeneratedValue
                private Integer id;

                @Enumerated(EnumType.STRING)
                private StaffRequestStatus staffStatus;

                @Enumerated(EnumType.STRING)
                private CustomerRequestStatus customerStatus;

                @Enumerated(EnumType.STRING)
                private PricingStatus pricingStatus;

                private Integer changedBy;

                private LocalDateTime changedAt;

                @ManyToOne
                private CarServiceRequest request;
        }
