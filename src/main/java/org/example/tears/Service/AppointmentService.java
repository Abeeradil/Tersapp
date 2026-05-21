package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.SlotDto;
import org.example.tears.Enums.AppointmentSlotStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Repository.AppointmentRepository;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final CarServiceRequestRepository requestRepository;

    private static final List<String> AVAILABLE_TIMES = List.of(
            "08:00","09:00","10:00","11:00",
            "12:00","16:00","17:00","18:00","19:00"
    );

    // ================= GET SLOTS =================
    public Map<String, Object> getAvailability(String date) {

        List<CarServiceRequest> requests =
                requestRepository.findByAppointmentDate(date);

        List<SlotDto> slots = new ArrayList<>();

        for (String time : AVAILABLE_TIMES) {

            SlotDto slot = new SlotDto();
            slot.setTime(time);

            CarServiceRequest match = requests.stream()
                    .filter(r -> time.equals(r.getAppointmentTime()))
                    .findFirst()
                    .orElse(null);

            if (match == null) {
                slot.setStatus(AppointmentSlotStatus.AVAILABLE);
            }
            else if (Boolean.TRUE.equals(match.getInitialPaid())
                    && Boolean.TRUE.equals(match.getFinalPaid())) {
                slot.setStatus(AppointmentSlotStatus.BOOKED);
            }
            else {
                slot.setStatus(AppointmentSlotStatus.PENDING);
            }

            slots.add(slot);
        }

        return Map.of(
                "date", date,
                "slots", slots
        );
    }

}