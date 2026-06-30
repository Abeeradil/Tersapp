package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.PricingStatus;

import java.time.LocalDateTime;

@Data
public class PricingRequestCardDto {

    private Integer id;
    private String orderNumber;
    private String customerName;
    private String carModel;
    private PricingStatus pricingStatus;
    private String problemDescription;
    private LocalDateTime assignedAt;

}
