package org.example.tears.Repository;

import org.example.tears.Enums.ReportVersionType;
import org.example.tears.Model.RequestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface RequestReportRepository extends JpaRepository<RequestReport,Integer> {

    Optional<RequestReport> findByRequest_IdAndLatestTrue(Integer requestId);

    Optional<RequestReport> findTopByRequest_IdAndCreatedBy_IdOrderByVersionDesc(
            Integer requestId,
            Integer employeeId
    );


    Optional<RequestReport> findTopByRequest_IdAndCreatedBy_IdAndVersionTypeOrderByVersionDesc(
            Integer requestId,
            Integer employeeId,
            ReportVersionType versionType
    );

    Optional<RequestReport> findByRequest_IdAndSentTrue(
            Integer requestId
    );
}
