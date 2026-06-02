package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WalletTransactionResponseDto {
    Integer id;
    Integer amount;
    Double amountSar;
    String type;
    String paymentMethod;
    String referenceNumber;
    String description;
    LocalDateTime createdAt;
}
