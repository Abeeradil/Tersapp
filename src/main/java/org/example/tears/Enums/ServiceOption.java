package org.example.tears.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceOption {


    FULL_MAINTENANCE("صيانة شاملة", 287),
    BODY_PAINT("سمكرة ودهان", 287),
    ELECTRONIC_CHECK("فحص إلكتروني", 1);//150

    private final String displayName;
    private final int price;

}
