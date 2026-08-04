package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.*;
import org.example.tears.Enums.WarrantyProblemType;
import org.example.tears.Enums.WarrantyStatus;

import java.time.LocalDateTime;
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
    private Employee assignedEmployee;

    @Enumerated(EnumType.STRING)
    private WarrantyStatus status;

    @Enumerated(EnumType.STRING)
    private WarrantyProblemType problemType;

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

    private LocalDateTime deliveredAt;
}