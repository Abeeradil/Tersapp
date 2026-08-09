package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tears.Enums.WarrantyImageType;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarrantyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private WarrantyRequest warrantyRequest;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private WarrantyImageType type;
}
