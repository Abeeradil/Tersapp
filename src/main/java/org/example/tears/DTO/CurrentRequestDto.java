package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyEligibilityStatus;
import org.example.tears.Enums.WarrantyReason;
import org.example.tears.Enums.WarrantyStatus;

@Data
public class CurrentRequestDto {

    private Integer id;

    private String orderNumber;

    private String serviceName;

    private String status;

    private Boolean warrantyRequest;
    private Integer warrantyId;
    private WarrantyReason warrantyReason;
    private WarrantyStatus warrantyStatus;
    private WarrantyEligibilityStatus warrantyEligibility;

    private String requestState;
}