package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletResponseDto {
    Integer walletId;
    Integer balance;
    Double balanceSar;
}
