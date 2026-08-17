package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.*;
import org.example.tears.Enums.WarrantyCustomerStatus;
import org.example.tears.Enums.WarrantyEligibilityStatus;
import org.example.tears.Enums.WarrantyReason;
import org.example.tears.Enums.WarrantyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarrantyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private CarServiceRequest request;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "assigned_technician_id")
    private Employee assignedTechnician;


    @Enumerated(EnumType.STRING)
    private WarrantyStatus status;

    @Enumerated(EnumType.STRING)
    private WarrantyCustomerStatus CustomerStatus;

    @Enumerated(EnumType.STRING)
    private WarrantyEligibilityStatus WarrantyEligibility;


    @Enumerated(EnumType.STRING)
    private WarrantyReason warrantyReason;

    private String rejectReason;

    @Lob
    private String description;

    private LocalDateTime createdAt;

    @ManyToOne
    private Employee approvedBy;

    private LocalDateTime approvedAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "warrantyRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<WarrantyImage> images = new ArrayList<>();

    private LocalDateTime repairStartedAt;

    private LocalDateTime repairCompletedAt;

    @OneToOne
    private Ticket ticket;

    private LocalDateTime deliveredAt;

    @ManyToOne
    @JoinColumn(name = "receiving_location_id")
    private Location receivingLocation;
    private LocalDate receivingDate;
    private LocalTime receivingTime;


    @ManyToOne
    @JoinColumn(name = "delivery_location_id")
    private Location deliveryLocation;
    private LocalDate deliveryDate;
    private LocalTime deliveryTime;
}