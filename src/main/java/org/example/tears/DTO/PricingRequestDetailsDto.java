package org.example.tears.DTO;

import lombok.Data;

import java.util.List;
@Data
public class PricingRequestDetailsDto {

                private Integer id;
                private String orderNumber;

                private String pricingStatus;
                private String serviceOption;

                // الفني
                private String technicianName;
                private String technicianPhone;

                // السيارة
                private String carModelName;
                private String carModelNameAr;

                private String plateNumberArabic;
                private String plateNumberEnglish;

                // الموقع
                private String address;

                // المشكلة
                private String problemDescription;

                private String technicianNote;

                // ملاحظات الفني
                private List<RequestNoteDTO> notes;


                // Timeline
                private List<TimelineItemDto> timeline;
}