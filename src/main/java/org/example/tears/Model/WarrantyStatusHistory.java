package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tears.Enums.WarrantyCustomerStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarrantyStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private WarrantyRequest warrantyRequest;

    @Enumerated(EnumType.STRING)
    private WarrantyCustomerStatus status;

    @ManyToOne
    @JoinColumn(name = "changed_by_employee_id")
    private Employee changedBy;

    private LocalDateTime changedAt;
}