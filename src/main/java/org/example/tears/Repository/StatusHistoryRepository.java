package org.example.tears.Repository;

import org.example.tears.Model.RequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<RequestStatusHistory,Integer> {

        List<RequestStatusHistory> findByRequestIdOrderByChangedAtDesc(Integer id);

}
