package org.example.tears.Mapper;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.PricingRequestCardDto;
import org.example.tears.DTO.PricingRequestDetailsDto;
import org.example.tears.DTO.RequestNoteDTO;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.RequestNote;
import org.example.tears.Repository.RequestNoteRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingRequestMapper {

    private final RequestNoteRepository noteRepo;


    public PricingRequestCardDto toPricingCardDto(CarServiceRequest request){

        PricingRequestCardDto dto = new PricingRequestCardDto();

        dto.setId(request.getId());
        dto.setOrderNumber(request.getOrderNumber());

        if(request.getPricingStatus()!=null){
            dto.setPricingStatus(request.getPricingStatus().name());
        }


        if(request.getCar()!=null){

            dto.setCarModelName(
                    request.getCar().getModel().getName()
            );

            dto.setCarModelNameAr(
                    request.getCar().getModel().getNameAr()
            );

            dto.setServiceOption(request.getServiceOption().name());

            dto.setAddress(request.getLocation().getAddress());

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

        RequestNote lastNote = noteRepo
                .findTopByRequestOrderByCreatedAtDesc(request);

        dto.setTechnicianNote(
                lastNote != null ? lastNote.getNote() : null
        );



        dto.setNotes(
                noteRepo.findByRequestOrderByCreatedAtDesc(request)
                        .stream()
                        .map(note -> {
                            RequestNoteDTO dtoNote = new RequestNoteDTO();

                            dtoNote.setNote(note.getNote());
                            dtoNote.setEmployeeName(
                                    note.getEmployee().getUser().getFullName()
                            );
                            dtoNote.setStep(note.getStep().name());
                            dtoNote.setCreatedAt(note.getCreatedAt());

                            return dtoNote;
                        })
                        .toList()
        );

        dto.setTechnicianName(
                request.getAssignedTechnician().getUser().getFullName()
        );

        dto.setServiceOption(
                request.getServiceOption().name()
        );

        dto.setTechnicianPhone(
                request.getAssignedTechnician().getUser().getPhoneNumber()
        );
        dto.setAddress(
                request.getLocation().getAddress()
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
