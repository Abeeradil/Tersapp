package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tears.Enums.WarrantyCustomerStatus;
import org.example.tears.Enums.WarrantyStatus;

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
    private WarrantyCustomerStatus customerStatus;

    @Enumerated(EnumType.STRING)
    private WarrantyStatus employeeStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by_employee_id", nullable = true)
    private Employee changedBy;

    private LocalDateTime changedAt;
}