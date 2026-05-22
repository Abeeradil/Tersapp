package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.tears.Enums.ServiceOption;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "coupons")
public class Coupon {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        // كود الكوبون
        @Column(unique = true, nullable = false)
        private String code;

        // نسبة خصم %
        private Integer discountPercentage;

        // خصم ثابت بالريال
        private Integer fixedDiscount;

        // أقل مبلغ مسموح لتطبيق الكوبون
        private Integer minimumOrderPrice;

        // أقصى خصم (لو نسبة)
        private Integer maxDiscountAmount;

        // هل الكوبون مفعل؟
        private boolean active = true;

        // تاريخ الانتهاء
        private LocalDate expiryDate;

        // عدد مرات الاستخدام المسموحة
        private Integer usageLimit;

        // عدد مرات الاستخدام الحالية
        private Integer usedCount = 0;

        // هل الكوبون لمستخدم واحد فقط؟
        private boolean oneTimePerUser = false;

        // هل الكوبون لخدمة معينة؟
        @Enumerated(EnumType.STRING)
        private ServiceOption serviceOption;

        private LocalDateTime createdAt;
    }