package org.example.tears.Service;

import lombok.AllArgsConstructor;
import org.example.tears.OutDTO.OutCarModelDTO;
import org.example.tears.Model.CarBrand;
import org.example.tears.Model.CarModel;
import org.example.tears.Repository.CarBrandRepository;
import org.example.tears.Repository.CarModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CarModelService {

        private final CarModelRepository carModelRepository;
        private final CarBrandRepository carBrandRepository;

    // 🔹 2. جلب موديلات براند واحد
        public List<OutCarModelDTO> getModelsByBrand(Integer brandId) {

            return carModelRepository.findByBrandId(brandId)
                    .stream()
                    .map(model -> new OutCarModelDTO(
                            model.getId(),
                            model.getNameAr()
                    ))
                    .toList();
        }

        public List<OutCarModelDTO> searchModels(Integer brandId, String nameEn,String nameAr) {
        List<CarModel> models = carModelRepository
                .findByBrandIdAndNameContainingIgnoreCaseOrBrandIdAndNameArContainingIgnoreCase(brandId, nameEn, brandId, nameAr);

        if (models.isEmpty()) {
            throw new RuntimeException("لا يوجد موديل بهذا الاسم للبراند المحدد");
        }

        return models.stream()
                .map(m -> new OutCarModelDTO(m.getId(), m.getName()))
                .toList();
    }
}

