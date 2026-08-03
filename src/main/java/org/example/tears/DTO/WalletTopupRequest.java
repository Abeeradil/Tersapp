package org.example.tears.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletTopupRequest {

    @NotNull
    @Min(100)
    private Integer amount; // بالهللة
}
