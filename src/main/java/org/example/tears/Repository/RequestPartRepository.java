package org.example.tears.Repository;

import org.example.tears.Model.RequestPart;
import org.example.tears.Model.RequestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestPartRepository extends JpaRepository<RequestPart,Integer> {
    List<RequestPart> findByRequestId(Integer requestId);
    List<RequestPart> findByReport_Id(Integer reportId);

    List<RequestPart> findByReportOrderById(RequestReport report);

    void deleteByRequestId(Integer requestId);
    List<RequestPart> findByRequestIdAndReportIsNull(Integer requestId);
    List<RequestPart> findByReport(RequestReport report);

    }
