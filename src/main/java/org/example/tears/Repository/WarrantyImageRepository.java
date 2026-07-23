package org.example.tears.Repository;

import org.example.tears.Model.WarrantyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyImageRepository extends JpaRepository<WarrantyImage, Integer> {

    List<WarrantyImage> findByWarrantyRequest_Id(Integer warrantyRequestId);

}