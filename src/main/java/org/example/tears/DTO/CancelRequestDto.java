package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.CancelReason;

@Data
public class CancelRequestDto {

private CancelReason reason;

private String otherReason;
}
