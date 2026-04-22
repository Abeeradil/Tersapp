package org.example.tears.Repository;

import org.example.tears.Model.PricingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingReportRepository extends JpaRepository<PricingReport,Integer> {

    Optional<PricingReport> findByRequestId(Integer id);
}
