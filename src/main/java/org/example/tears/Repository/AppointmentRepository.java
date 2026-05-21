package org.example.tears.Repository;

import org.example.tears.Model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

        boolean existsByDateAndTime(String date, String time);

        List<Appointment> findByDate(String date);

        @Query("SELECT a.time FROM Appointment a WHERE a.date = :date")
        List<String> findBookedTimesByDate(String date);
    }