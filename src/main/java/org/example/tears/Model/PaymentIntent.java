package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tears.Enums.PaymentStatus;
import org.example.tears.Enums.ServiceOption;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "payment_intents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 👤 المستخدم
    @ManyToOne
    private Customer customer;

    // 🚗 بيانات الطلب (مؤقتة)
    private Integer carId;

    @Enumerated(EnumType.STRING)
    private ServiceOption serviceOption;

    @Column(columnDefinition = "TEXT")
    private String problemDescription;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private Boolean hydraulicTruck;

    @ManyToOne
    private Location location;

    // 💰 السعر المتوقع
    private Integer estimatedPrice;

    // 💳 الدفع
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String paymentMethod;

    private String couponCode;

    // Moyasar
    private String paymentId;

    private String invoiceId;
    private String checkoutUrl;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
}