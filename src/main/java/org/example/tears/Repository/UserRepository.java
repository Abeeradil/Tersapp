package org.example.tears.Repository;

import org.example.tears.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

     void deleteByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumber(String phone);

    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    boolean existsByPhoneNumber(String phone);

}
