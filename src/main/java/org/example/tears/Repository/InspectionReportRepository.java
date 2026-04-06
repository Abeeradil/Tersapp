package org.example.tears.Repository;

import org.example.tears.Model.InspectionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InspectionReportRepository extends JpaRepository<InspectionReport,Integer> {
    Optional<InspectionReport> findByRequestId(Integer id);
}