package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code;  // الكود الذي يدخله المستخدم

    @Column(nullable = false)
    private int discount;  // قيمة الخصم بالريال

    private boolean active = true; // لتفعيل/تعطيل الكوبون
}
