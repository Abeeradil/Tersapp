package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.SlotDto;
import org.example.tears.Repository.AppointmentRepository;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentService {

        private final AppointmentRepository appointmentRepository;

        private static final List<String> AVAILABLE_TIMES = List.of(
                "08:00","09:00","10:00","11:00",
                "12:00","16:00","17:00","18:00","19:00"
        );

        // ================= VALIDATION =================
        public void validateAppointment(String date, String time) {

            if (!AVAILABLE_TIMES.contains(time)) {
                throw new RuntimeException("المواعيد كل ساعة فقط");
            }

            boolean exists = appointmentRepository
                    .existsByDateAndTime(date, time);

            if (exists) {
                throw new RuntimeException("هذا الموعد محجوز");
            }
        }

        // ================= AVAILABILITY =================
        public Map<String, Object> getAvailability(String date) {

            List<String> booked = appointmentRepository
                    .findBookedTimesByDate(date);

            List<SlotDto> slots = AVAILABLE_TIMES.stream()
                    .map(time -> {
                        SlotDto dto = new SlotDto();
                        dto.setTime(time);
                        dto.setAvailable(!booked.contains(time));
                        return dto;
                    })
                    .toList();

            return Map.of(
                    "date", date,
                    "slots", slots
            );
        }

        // ================= WORKING HOURS =================
        public Map<String, Object> getWorkingHours() {

            return Map.of(
                    "workingDays", "Sunday - Thursday",
                    "availableTimes", AVAILABLE_TIMES,
                    "supportedCities", List.of("Makkah", "Jeddah")
            );
        }

}