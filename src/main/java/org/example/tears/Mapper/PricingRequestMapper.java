package org.example.tears.Mapper;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.PricingRequestCardDto;
import org.example.tears.DTO.PricingRequestDetailsDto;
import org.example.tears.DTO.RequestNoteDTO;
import org.example.tears.DTO.TimelineItemDto;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.RequestNote;
import org.example.tears.Repository.RequestNoteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

        dto.setTimeline(buildTimeline(request)
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

                            dtoNote.setStep(
                                    note.getStep()
                            );
                            dtoNote.setCreatedAt(note.getCreatedAt());

                            return dtoNote;
                        })
                        .toList()
        );


        dto.setTechnicianName(
                request.getAssignedEmployee().getUser().getFullName()
        );

        dto.setServiceOption(
                request.getServiceOption().name()
        );

        dto.setTechnicianPhone(
                request.getAssignedEmployee().getUser().getPhoneNumber()
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

    private List<TimelineItemDto> buildTimeline(CarServiceRequest r){

        List<TimelineItemDto> list = new ArrayList<>();

        list.add(new TimelineItemDto(
                "تم إنشاء الطلب",
                StaffRequestStatus.NEW,
                r.getCreatedAt(),
                r.getCreatedAt() != null,
                r.getStaffStatus() == StaffRequestStatus.NEW
        ));

        list.add(new TimelineItemDto(
                "تم استلام السيارة",
                StaffRequestStatus.RECEIVED,
                r.getReceivedAt(),
                r.getReceivedAt() != null,
                r.getStaffStatus() == StaffRequestStatus.RECEIVED
        ));

        list.add(new TimelineItemDto(
                "جاري الفحص",
                StaffRequestStatus.INSPECTION_IN_PROGRESS,
                r.getInspectionAt(),
                r.getInspectionAt() != null,
                r.getStaffStatus() == StaffRequestStatus.INSPECTION_IN_PROGRESS
        ));

        list.add(new TimelineItemDto(
                "قيد التجربة",
                StaffRequestStatus.TESTING,
                r.getTestingAt(),
                r.getTestingAt() != null,
                r.getStaffStatus() == StaffRequestStatus.TESTING
        ));

        list.add(new TimelineItemDto(
                "تسجيل القطع",
                StaffRequestStatus.PARTS_REGISTERING,
                r.getPartsRegisteredAt(),
                r.getPartsRegisteredAt() != null,
                r.getStaffStatus() == StaffRequestStatus.PARTS_REGISTERING
        ));

        list.add(new TimelineItemDto(
                "جاري التسعير",
                StaffRequestStatus.PRICING,
                r.getPricingAt(),
                r.getPricingAt() != null,
                r.getStaffStatus() == StaffRequestStatus.PRICING
        ));

        list.add(new TimelineItemDto(
                "جاري الإصلاح",
                StaffRequestStatus.REPAIRING,
                r.getRepairAt(),
                r.getRepairAt() != null,
                r.getStaffStatus() == StaffRequestStatus.REPAIRING
        ));

        list.add(new TimelineItemDto(
                "تم التسليم",
                StaffRequestStatus.DELIVERED,
                r.getDeliveredAt(),
                r.getDeliveredAt() != null,
                r.getStaffStatus() == StaffRequestStatus.DELIVERED
        ));

        return list;
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
