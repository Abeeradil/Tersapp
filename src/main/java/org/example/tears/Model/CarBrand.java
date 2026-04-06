package org.example.tears.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Setter@Getter
@Table(name = "car_brands")
public class CarBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // اسم العرض (للمستخدم)
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String nameAr;

    // الاسم التقني (للصور والروابط)
    @Column(nullable = false, unique = true)
    private String slug;

    private String logoPath;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CarModel> models;

}
