package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tears.Enums.StaffRequestStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private CarServiceRequest request;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private StaffRequestStatus uploadedAtStatus;

    private LocalDateTime uploadedAt;
}