package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Entity
public class RequestPart {
        @Id
        @GeneratedValue
        private Integer id;

        private String name;             // اسم القطعة
        private String type;             // نوعها / تصنيفها
        private Integer quantity;        // عدد القطع
        private Integer estimatedPrice;  // السعر التقديري لكل وحدة
        private Integer finalPrice;      // السعر النهائي بعد الموافقة
        private Integer laborCost;       // تكلفة التركيب / العمل

        @ManyToOne
        @JoinColumn(name = "request_id")
        private CarServiceRequest request;
    }
