package org.example.tears.Model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.tears.Enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CarServiceRequest {

                @Id
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                private Integer id;

                // ========================
                // Order Info
                // ========================
                private String orderNumber;


                @ManyToOne
                @JoinColumn(name = "car_id")
                private Car car;

                @Enumerated(EnumType.STRING)
                private ServiceOption serviceOption;

                @Enumerated(EnumType.STRING)
                private PaymentStatus paymentStatus;

                @Column(columnDefinition = "TEXT")
                private String problemDescription;

                @NotNull
                private boolean hydraulicTruck;

                @ManyToOne
                private Customer customer;

                @ManyToOne
                @JoinColumn(name = "coupon_id")
                private Coupon coupon;

                @Column(name = "received_image")
                private String receivedImageUrl;  // رابط الصورة بعد الرفع

                // ========================
                // Appointment
                // ========================
                private LocalDate appointmentDate;
                private LocalTime appointmentTime;

                // ========================
                // Pricing & Payment
                // ========================
                // الأسعار
                private Double estimatedPrice;    // السعر التقديري للطلب الأساسي (مثلاً سعر الخدمة + السطحه)
                private Integer finalPrice;        // السعر النهائي بعد تسعير القطع والإصلاحات


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

                // الدفع
                private boolean initialPaid = false;       // هل دفعت المرحلة الأولى؟
                private boolean finalPaid = false; // هل دفعت المرحلة الثانية؟

                private String initialTransactionId; // رقم العملية الأولى
                private String finalTransactionId;   // رقم العملية الثانية



                @Enumerated(EnumType.STRING)
                private PaymentMethod paymentMethod;

                // ========================
                // Status
                // ========================
                @Enumerated(EnumType.STRING)
                private StaffRequestStatus staffStatus;

                @Enumerated(EnumType.STRING)
                private CustomerRequestStatus customerStatus;

                @Enumerated(EnumType.STRING)
                private PricingStatus pricingStatus;

                // المرحلة الحالية
                @Enumerated(EnumType.STRING)
                private WorkflowStage stage;


                // ========================
                // Assignment
                // ========================
                @ManyToOne
                @JoinColumn(name = "assigned_employee_id")
                private Employee assignedEmployee;

                @ManyToOne
                @JoinColumn(name = "assigned_pricing_id")
                private Employee assignedPricingEmployee;

                @ManyToOne
                @JoinColumn(name = "current_employee_id")
                private Employee currentEmployee;

                // ========================
                // Timeline
                // ========================
                private LocalDateTime createdAt;
                private LocalDateTime receivedAt;
                private LocalDateTime inspectionAt;
                private LocalDateTime testingAt;
                private LocalDateTime pricingAt;
                private LocalDateTime repairAt;
                private LocalDateTime deliveredAt;
                private LocalDateTime lastUpdated;
                // ========================
                // Location
                // ========================
                @ManyToOne
                @JoinColumn(name = "location_id")
                private Location location;

    private String paymentId;

        }
