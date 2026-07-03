package org.example.tears.Repository;

import jdk.jfr.Registered;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Notification;
import org.example.tears.Model.RequestNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestNoteRepository
        extends JpaRepository<RequestNote, Integer> {

    List<RequestNote> findByRequestOrderByCreatedAtDesc(CarServiceRequest request);
}
