package org.example.tears.Repository;

import org.example.tears.Model.Customer;
import org.example.tears.Model.RequestApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestApprovalRepository extends JpaRepository<RequestApproval, Integer> {
    Optional<RequestApproval> findByRequest_Id(Integer requestId);

    List<RequestApproval> findByRequest_CustomerAndApproved(
            Customer customer,
            Boolean approved
    );

    List<RequestApproval> findByRequest_CustomerAndApprovedIsNull(
            Customer customer
    );

    List<RequestApproval> findByRequest_Customer(Customer customer);
}