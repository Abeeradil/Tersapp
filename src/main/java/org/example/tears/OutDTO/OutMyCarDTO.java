package org.example.tears.OutDTO;
import lombok.Data;

@Data
public class OutMyCarDTO {

    private Integer carId;

    private String plateNumberArabic;

    private String brandNameAr;
    private String modelNameAr;

    private Integer carYear;
    private String carImage;   // صورة الموديل أو السيارة
}
