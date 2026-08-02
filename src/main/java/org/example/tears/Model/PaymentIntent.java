package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tears.Enums.PaymentMethod;
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
    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;


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
    private Double estimatedPrice;

    // 💳 الدفع
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String couponCode;


    @OneToOne
    private CarServiceRequest serviceRequest;


    private String invoiceId;
    private String checkoutUrl;
    private LocalDateTime expiresAt;
    private LocalDateTime paidAt;

    // Moyasar
    private String paymentId;
    private LocalDateTime createdAt;
    private String givenId;


    private Double originalPrice;

    private Double discount;

    private Double vatAmount;

    private Boolean couponValid;

    private String pricingMessage;

    private Double initialPaymentAmount;

    private Integer initialPaymentAmountHalalah;

    @Enumerated(EnumType.STRING)
    private PaymentMethod initialPaymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus initialPaymentStatus;

    private Double remainingAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod nextPaymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus nextPaymentStatus;

}