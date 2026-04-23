package org.example.tears.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Min(value = 1900, message = "Year must be valid")
    @Max(value = 2027, message = "Year must be valid")
    @Column(name = "car_year")
    private Integer carYear;


    @NotBlank(message = "Plate number in Arabic is required")
    @Column(unique = true ,nullable = false)
    private String plateNumberArabic;

    @Column(nullable = false)
    private String plateNumberEnglish;

    @Min(value = 0, message = "Mileage must be positive")
    private Integer mileage;

    @Column(nullable = true)
    private String formImagePath;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private CarBrand brand;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private CarModel model;

}
