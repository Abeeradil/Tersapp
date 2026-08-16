package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyCustomerStatus;
import org.example.tears.Enums.WarrantyEligibilityStatus;
import org.example.tears.Enums.WarrantyReason;

@Data
public class CurrentRequestDto {

    private Integer id;

    private String orderNumber;

    private String serviceName;

    private String status;

    private Boolean warrantyRequest;
    private Integer warrantyId;
    private WarrantyReason warrantyReason;
    private WarrantyCustomerStatus warrantyStatus;
    private WarrantyEligibilityStatus warrantyEligibility;

    private String requestState;
}