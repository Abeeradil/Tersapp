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


                @NotNull
                private Integer carId;

                @Enumerated(EnumType.STRING)
                private ServiceOption serviceOption;

                @Column(columnDefinition = "TEXT")
                private String problemDescription;

                @NotNull
                private boolean hydraulicTruck;

                @ManyToOne
                private Customer customer;

                @Column(name = "received_image")
                private String receivedImageUrl;  // رابط الصورة بعد الرفع




    // ========================
                // Appointment
                // ========================
                private String appointmentDate;
                private String appointmentTime;

                // ========================
                // Pricing & Payment
                // ========================
                // الأسعار
                private Integer estimatedPrice;    // السعر التقديري للطلب الأساسي (مثلاً سعر الخدمة + السطحه)
                private Integer finalPrice;        // السعر النهائي بعد تسعير القطع والإصلاحات
                private boolean paid;
                private String paymentTransactionId;

                // الدفع
                private boolean initialPaid;       // هل دفعت المرحلة الأولى؟
                private boolean finalPaid;         // هل دفعت المرحلة الثانية؟
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

        }
