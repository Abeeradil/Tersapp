package org.example.tears.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceOption {


    WARRANTY("طلب تحت الضمان",0),
    FULL_MAINTENANCE("صيانة شاملة", 250),
    BODY_PAINT("سمكرة ودهان", 250),
    ELECTRONIC_CHECK("فحص إلكتروني", 150);

    private final String displayName;
    private final int price;



}
