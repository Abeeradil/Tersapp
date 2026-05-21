package org.example.tears.OutDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutCarDetailsDTO {

    private Integer carId;

    private String ownerName;

    private String brandName;
    private String brandNameAr;

    private String modelName;
    private String modelNameAr;

    private String plateNumberArabic;
    private String plateNumberEnglish;

    private Integer carYear;

    private Integer mileage;

    private String formImagePath;
}