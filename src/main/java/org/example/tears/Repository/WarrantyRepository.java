package org.example.tears.Repository;

import org.example.tears.Model.WarrantyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyRepository extends JpaRepository<WarrantyRequest, Integer> {

    List<WarrantyRequest> findByCustomer_IdOrderByCreatedAtDesc(Integer customerId);

}