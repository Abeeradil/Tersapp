package org.example.tears.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class IstimaraData {

    private String plate_number;

    private String plate_text_ar;

    private String plate_text_en;

    private String vehicle_make;

    private String vehicle_model;

    private String model_year;

    private String color;

    private String vin;

    private String owner_name;
}