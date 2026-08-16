package org.example.tears.Repository;

import org.example.tears.Model.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository
        extends JpaRepository<CouponUsage, Integer> {

    long countByCouponId(Integer couponId);

    boolean existsByCouponIdAndCustomerId(
            Integer couponId,
            Integer customerId
    );

    boolean existsByCouponIdAndCustomerIdAndRequestId(
            Integer couponId,
            Integer customerId,
            Integer requestId
    );
}
