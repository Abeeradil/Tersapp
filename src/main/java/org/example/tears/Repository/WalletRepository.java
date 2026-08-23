package org.example.tears.Repository;

import jakarta.persistence.LockModeType;
import org.example.tears.Model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByUserId(Integer userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT w
    FROM Wallet w
    WHERE w.user.id = :userId
""")
    Optional<Wallet> findByUserIdForUpdate(
            @Param("userId") Integer userId
    );

}