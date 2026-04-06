package org.example.tears.Repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.example.tears.Model.PaymentGatewaySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentGatewaySettingRepository extends JpaRepository<PaymentGatewaySetting, Integer> {

    Optional<PaymentGatewaySetting> findByProvider(String provider);


}
