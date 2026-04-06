package org.example.tears.Repository;

import org.example.tears.Model.RequestAssignment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestAssignmentRepository extends CrudRepository<RequestAssignment, Integer> {
}
