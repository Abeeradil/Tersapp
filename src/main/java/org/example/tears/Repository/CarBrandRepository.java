package org.example.tears.Repository;

import org.example.tears.Model.CarBrand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarBrandRepository extends JpaRepository<CarBrand , Integer> {
 Optional<CarBrand> findByNameIgnoreCase(String brandName);

 List<CarBrand> findByNameContainingIgnoreCaseOrNameArContainingIgnoreCase(String nameEn, String nameAr);
 List<CarBrand> findByNameArStartingWithIgnoreCase(String letter);


}
