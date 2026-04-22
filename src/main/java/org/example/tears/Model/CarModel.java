package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "car_models")
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer id;
    // اسم العرض
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nameAr;


    // الاسم التقني
    @Column(nullable = false, unique = true)
    private String slug;

    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private CarBrand brand;
}
