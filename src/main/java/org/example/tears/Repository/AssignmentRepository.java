package org.example.tears.Repository;

import org.example.tears.Model.Assignment;
import org.example.tears.Service.AssignmentService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
        @Modifying
        @Query("UPDATE RequestAssignment r SET r.status = 'CLOSED' WHERE r.request.id = :requestId AND r.status = 'ACTIVE'")
        void closeActive(Integer requestId);
    }
