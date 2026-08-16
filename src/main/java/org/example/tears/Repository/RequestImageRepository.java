package org.example.tears.Repository;

import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.RequestImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface
RequestImageRepository
        extends JpaRepository<RequestImage,Integer> {

    List<RequestImage> findByRequest_Id(Integer requestId);

    long countByRequest(CarServiceRequest request);




    List<RequestImage> findByRequestIdAndVisibleToCustomerTrue(
            Integer requestId
    );
}