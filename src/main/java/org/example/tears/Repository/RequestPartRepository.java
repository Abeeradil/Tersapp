package org.example.tears.Repository;

import org.example.tears.Model.RequestPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestPartRepository extends JpaRepository<RequestPart,Integer> {
    List<RequestPart> findByRequestId(Integer requestId);

    RequestPart deleteByRequestId(Integer requestId);


    }
