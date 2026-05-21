package org.example.tears.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Entity
    @Data
    public class Appointment {

        @Id
        @GeneratedValue
        private Integer id;

        private LocalDate date;

        private String time;

        @ManyToOne
        private CarServiceRequest request;

        @ManyToOne
        private Employee assignedEmployee;

        private boolean confirmed = false;
    }
