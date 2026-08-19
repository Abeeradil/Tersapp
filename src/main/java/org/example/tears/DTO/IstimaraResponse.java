package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IstimaraResponse {

    private boolean success;

    private IstimaraData data;

    private Quality quality;
}
