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

    // ================= AVAILABILITY =================
    public Map<String, Object> getAvailability(String date) {

        // كل الطلبات في هذا اليوم
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
            } else if (!match.getInitialPaid()) {
                slot.setStatus(AppointmentSlotStatus.PENDING);
            } else {
                slot.setStatus(AppointmentSlotStatus.BOOKED);
            }

            slots.add(slot);
        }

        long available = slots.stream()
                .filter(s -> s.getStatus() == AppointmentSlotStatus.AVAILABLE)
                .count();

        long pending = slots.stream()
                .filter(s -> s.getStatus() == AppointmentSlotStatus.PENDING)
                .count();

        long booked = slots.stream()
                .filter(s -> s.getStatus() == AppointmentSlotStatus.BOOKED)
                .count();

        return Map.of(
                "date", date,
                "summary", Map.of(
                        "available", available,
                        "pending", pending,
                        "booked", booked
                ),
                "slots", slots
        );
    }

    // ================= WORKING HOURS =================
    public Map<String, Object> getWorkingHours() {

        return Map.of(
                "workingDays", "Sunday - Thursday",
                "availableTimes", AVAILABLE_TIMES
        );
    }

}