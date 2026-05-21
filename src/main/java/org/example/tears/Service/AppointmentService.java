package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final CarServiceRequestRepository requestRepository;

    private static final List<String> AVAILABLE_TIMES = List.of(
            "08:00",
            "09:00",
            "10:00",
            "11:00",
            "12:00",
            "16:00",
            "17:00",
            "18:00",
            "19:00"
    );

    // ---------------------------
    // Validate Appointment
    // ---------------------------
    public void validateAppointment(String date, String time) {

        if (!AVAILABLE_TIMES.contains(time)) {
            throw new RuntimeException("المواعيد المتاحة كل ساعة فقط");
        }

        int count = requestRepository
                .countByAppointmentDateAndAppointmentTime(date, time);

        if (count >= 1) {
            throw new RuntimeException("هذا الموعد محجوز");
        }
    }

    // ---------------------------
    // Availability
    // ---------------------------
    public Map<String, Object> getAvailability(String date) {

        List<String> booked = requestRepository
                .findBookedTimesByDate(date);

        List<Map<String, Object>> slots = AVAILABLE_TIMES.stream()
                .map(time -> {

                    boolean isAvailable = !booked.contains(time);

                    return Map.<String, Object>of(
                            "time", time,
                            "available", isAvailable
                    );
                })
                .toList();

        return Map.of(
                "date", date,
                "slots", slots
        );
    }

    // ---------------------------
    // Working Hours
    // ---------------------------
    public Map<String, Object> getWorkingHours() {

        return Map.of(
                "workingDays", "Sunday - Thursday",
                "availableTimes", AVAILABLE_TIMES,
                "supportedCities", List.of("Makkah", "Jeddah")
        );
    }
}