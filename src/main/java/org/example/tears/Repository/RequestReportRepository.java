package org.example.tears.Repository;

import org.example.tears.Model.RequestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestReportRepository extends JpaRepository<RequestReport,Integer> {
}
