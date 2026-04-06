package org.example.tears.Repository;

import org.example.tears.Model.CarBrand;
import org.example.tears.Model.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarModelRepository extends JpaRepository<CarModel,Integer> {

    Optional<CarModel> findByNameIgnoreCaseAndBrandId(String name, Integer brandId);
    List<CarModel> findByNameArContainingIgnoreCase(String keyword);
    List<CarModel> findByNameArStartingWithIgnoreCase(String letter);
    List<CarModel> findByBrandIdAndNameContainingIgnoreCaseOrBrandIdAndNameArContainingIgnoreCase(
            Integer brandId1, String nameEn,
            Integer brandId2, String nameAr
    );
    List<CarModel>findByBrandId(Integer brandId);

}
