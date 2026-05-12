package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Model.CarModel;
import org.example.tears.OutDTO.OutCarBrandDTO;
import org.example.tears.Model.CarBrand;
import org.example.tears.OutDTO.OutCarModelDTO;
import org.example.tears.Repository.CarBrandRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarBrandService {

        private final CarBrandRepository carBrandRepository;

        public List<OutCarBrandDTO> getAllBrands() {
                return carBrandRepository.findAll().stream()
                        .map(b -> new OutCarBrandDTO(b.getId(), b.getName(), b.getNameAr(), b.getLogoPath()))
                        .toList();
        }

        // البحث بالاسم (عربي/إنجليزي)
        public List<OutCarBrandDTO> searchBrands(
                String keyword,
                String sort
        ) {

                List<CarBrand> brands = carBrandRepository.findAll();

                // SEARCH
                if (keyword != null && !keyword.isBlank()) {

                        String k = keyword.toLowerCase();

                        brands = brands.stream()
                                .filter(b ->

                                        b.getName().toLowerCase().contains(k)

                                                ||

                                                b.getNameAr().contains(keyword)
                                )
                                .toList();
                }

                // SORT
                Comparator<CarBrand> comparator =
                        Comparator.comparing(CarBrand::getName);

                if ("desc".equalsIgnoreCase(sort)) {
                        comparator = comparator.reversed();
                }

                return brands.stream()
                        .sorted(comparator)
                        .map(this::convertToDTO)
                        .toList();
        }

        private OutCarBrandDTO convertToDTO(CarBrand brand) {

                OutCarBrandDTO dto = new OutCarBrandDTO();

                dto.setId(brand.getId());
                dto.setName(brand.getName());
                dto.setNameAr(brand.getNameAr());
                dto.setLogoPath(brand.getLogoPath());

                return dto;
        }


        // إضافة ماركة جديدة
        public CarBrand addBrand(CarBrand brand) {
            return carBrandRepository.save(brand);
        }

        // حذف ماركة
        public void deleteBrand(Integer id) {
            carBrandRepository.deleteById(id);
        }


        // جلب ماركة بالآيدي
        public CarBrand getBrandById(Integer id) {
            return carBrandRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
        }

}
