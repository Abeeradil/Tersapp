package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.SlotDto;
import org.example.tears.Enums.AppointmentSlotStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Repository.AppointmentRepository;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final CarServiceRequestRepository requestRepository;

    private static final List<LocalTime> AVAILABLE_TIMES = List.of(
            LocalTime.of(8,0),
            LocalTime.of(9,0),
            LocalTime.of(10,0),
            LocalTime.of(11,0),
            LocalTime.of(12,0),
            LocalTime.of(16,0),
            LocalTime.of(17,0),
            LocalTime.of(18,0),
            LocalTime.of(19,0)
    );
    public void validateAppointment(LocalDate date, LocalTime time) {

        if (!AVAILABLE_TIMES.contains(time)) {
            throw new RuntimeException("المواعيد كل ساعة فقط");
        }

        boolean exists = requestRepository
                .existsByAppointmentDateAndAppointmentTime(date, time);

        if (exists) {
            throw new RuntimeException("هذا الموعد محجوز");
        }
    }

    public Map<String, Object> getAvailability(LocalDate date) {

        List<CarServiceRequest> requests =
                requestRepository.findByAppointmentDate(date);

        List<SlotDto> slots = new ArrayList<>();

        for (LocalTime time : AVAILABLE_TIMES) {

            SlotDto slot = new SlotDto();
            slot.setTime(time);

            CarServiceRequest match = requests.stream()
                    .filter(r -> time.equals(r.getAppointmentTime()))
                    .findFirst()
                    .orElse(null);

            if (match == null) {
                slot.setStatus(AppointmentSlotStatus.AVAILABLE);
            }
            else if (Boolean.TRUE.equals(match.getInitialPaid())) {
                slot.setStatus(AppointmentSlotStatus.BOOKED);
            }
            else {
                slot.setStatus(AppointmentSlotStatus.PENDING);
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


}