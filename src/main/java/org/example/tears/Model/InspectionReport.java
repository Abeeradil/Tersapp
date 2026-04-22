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
public class InspectionReport {


        @Id
        @GeneratedValue
        private Integer id;

        @OneToOne
        private CarServiceRequest request;

        @Column(length = 2000)
        private String problems;

        private String imageUrl;

        private LocalDateTime createdAt;

}
