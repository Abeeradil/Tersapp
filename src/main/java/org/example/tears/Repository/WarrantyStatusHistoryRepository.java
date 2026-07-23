package org.example.tears.Repository;

import org.example.tears.Model.WarrantyStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyStatusHistoryRepository
        extends JpaRepository<WarrantyStatusHistory, Integer> {

    List<WarrantyStatusHistory>
    findByWarrantyRequest_IdOrderByChangedAtDesc(Integer warrantyRequestId);

}