package org.example.tears.Repository;

import org.example.tears.Model.Location;
import org.example.tears.OutDTO.OutLocationDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location,Integer> {
    List<Location> findByCustomerId(Integer customerId);
}
