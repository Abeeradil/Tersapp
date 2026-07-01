package org.example.tears.Service;

import org.example.tears.DTO.PricingRequestCardDto;
import org.example.tears.DTO.PricingRequestDetailsDto;
import org.example.tears.Model.CarServiceRequest;
import org.springframework.stereotype.Service;

@Service
public class PricingRequestMapper {

    public PricingRequestCardDto toPricingCardDto(CarServiceRequest request){

        PricingRequestCardDto dto = new PricingRequestCardDto();

        dto.setId(request.getId());
        dto.setOrderNumber(request.getOrderNumber());

        if(request.getPricingStatus()!=null){
            dto.setPricingStatus(request.getPricingStatus().name());
        }

        if(request.getCustomer()!=null){
            dto.setCustomerName(
                    request.getCustomer().getUser().getFullName()
            );
        }

        if(request.getCar()!=null){

            dto.setCarModelName(
                    request.getCar().getModel().getName()
            );

            dto.setCarModelNameAr(
                    request.getCar().getModel().getNameAr()
            );

            dto.setPlateNumberArabic(
                    formatArabicPlate(
                            request.getCar().getPlateNumberArabic()
                    )
            );

            dto.setPlateNumberEnglish(
                    formatEnglishPlate(
                            request.getCar().getPlateNumberEnglish()
                    )
            );
        }

        dto.setCreatedAt(request.getCreatedAt());

        return dto;
    }

    public PricingRequestDetailsDto toPricingDetailsDto(CarServiceRequest request){

        PricingRequestDetailsDto dto = new PricingRequestDetailsDto();

        dto.setId(request.getId());
        dto.setOrderNumber(request.getOrderNumber());

        dto.setPricingStatus(
                request.getPricingStatus().name()
        );

        dto.setProblemDescription(
                request.getProblemDescription()
        );

        dto.setCustomerName(
                request.getCustomer().getUser().getFullName()
        );

        dto.setCustomerPhone(
                request.getCustomer().getUser().getPhoneNumber()
        );

        dto.setCarModelName(
                request.getCar().getModel().getName()
        );

        dto.setCarModelNameAr(
                request.getCar().getModel().getNameAr()
        );

        dto.setPlateNumberArabic(
                formatArabicPlate(
                        request.getCar().getPlateNumberArabic()
                )
        );

        dto.setPlateNumberEnglish(
                formatEnglishPlate(
                        request.getCar().getPlateNumberEnglish()
                )
        );

        return dto;
    }

    private String formatEnglishPlate(String plate) {

        if (plate == null || plate.length() < 4) {
            return plate;
        }

        String letters = plate.substring(0, 3);
        String numbers = plate.substring(3);

        return letters + "-" + numbers;
    }

    private String formatArabicPlate(String plate) {

        if (plate == null || plate.isBlank()) {
            return plate;
        }

        String[] parts = plate.trim().split("\\s+");

        if (parts.length == 4) {
            return parts[0] + " " + parts[1] + " " + parts[2] + " - " + parts[3];
        }

        return plate;
    }
}
