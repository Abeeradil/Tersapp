package org.example.tears.OutDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.tears.InpDTO.LocationDto;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Setter
@Getter
public class RequestResponseDto {

        private Integer id;

        private String orderNumber;

        private String status;

        private Boolean warrantyRequest;
        private String warrantyStatus;

        private Double totalPrice;

        private LocalDate appointmentDate;
        private LocalTime appointmentTime;

        private boolean hydraulicTruck;

        private String paymentMethod;

        private String paymentStatus;

        private String plateNumberArabic;
        private String plateNumberEnglish;

        private LocationDto location;

        private Double originalPrice;

        private Double discount;

        private Double vatAmount;

        private Boolean couponValid;

        private String pricingMessage;

        private Double amountPaid;

        private Integer amountPaidHalalah;

        private String initialPaymentMethod;

        private String initialPaymentStatus;

        private Double remainingAmount;

        private String nextPaymentMethod;

        private String nextPaymentStatus;
}