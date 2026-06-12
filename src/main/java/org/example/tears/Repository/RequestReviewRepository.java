package org.example.tears.Repository;

import org.example.tears.Model.RequestReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestReviewRepository extends JpaRepository<RequestReview,Integer> {

    boolean existsByRequestId(Integer requestId);
}