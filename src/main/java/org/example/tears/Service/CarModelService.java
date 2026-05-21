package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.OutDTO.OutCarModelDTO;
import org.example.tears.Model.CarBrand;
import org.example.tears.Model.CarModel;
import org.example.tears.Repository.CarBrandRepository;
import org.example.tears.Repository.CarModelRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarModelService {

        private final CarModelRepository carModelRepository;
        private final CarBrandRepository carBrandRepository;

    // 🔹 2. جلب موديلات براند واحد
        public List<OutCarModelDTO> getModelsByBrand(Integer brandId) {

            return carModelRepository.findByBrandId(brandId)
                    .stream()
                    .map(model -> new OutCarModelDTO(
                            model.getId(),
                            model.getNameAr(),
                            model.getName()

                    ))
                    .toList();
        }

    public List<OutCarModelDTO> searchModels(
            Integer brandId,
            String keyword,
            String sort
    ) {

        List<CarModel> models =
                carModelRepository.findByBrandId(brandId);

        // SEARCH
        if (keyword != null && !keyword.isBlank()) {

            String k = keyword.toLowerCase();

            models = models.stream()
                    .filter(m ->
                            m.getName().toLowerCase().contains(k)
                                    || m.getNameAr().contains(keyword)
                    )
                    .toList();
        }

        // SORT
        Comparator<CarModel> comparator =
                Comparator.comparing(CarModel::getName);

        if ("desc".equalsIgnoreCase(sort)) {
            comparator = comparator.reversed();
        }

        return models.stream()
                .sorted(comparator)
                .map(this::convertToDTO)
                .toList();
    }

    private OutCarModelDTO convertToDTO(CarModel model) {

        OutCarModelDTO dto = new OutCarModelDTO();

        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setNameAr(model.getNameAr());

        return dto;
    }
}

