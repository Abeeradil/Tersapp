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
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                private Integer id;

                private String name;

                private String type;

                private Integer quantity;
                private String problemDescription;


    // يدخلها موظف الورشة بالبداية
                private Integer estimatedPrice;

                // يعدلها موظف التسعير
                private Integer finalPrice;

                // محسوبة تلقائي
                private Integer laborCost;

                private Boolean priced = false;

                @ManyToOne
                @JoinColumn(name = "request_id")
                private CarServiceRequest request;
        }