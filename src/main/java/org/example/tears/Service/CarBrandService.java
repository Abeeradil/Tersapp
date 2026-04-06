package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.OutDTO.OutCarBrandDTO;
import org.example.tears.Model.CarBrand;
import org.example.tears.Repository.CarBrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarBrandService {

        private final CarBrandRepository carBrandRepository;

        public List<OutCarBrandDTO> getAllBrands() {
                return carBrandRepository.findAll().stream()
                        .map(b -> new OutCarBrandDTO(b.getId(), b.getName(), b.getLogoPath()))
                        .toList();
        }

        // البحث بالاسم (عربي/إنجليزي)
        public List<OutCarBrandDTO> searchBrands(String name) {
                List<CarBrand> brands = carBrandRepository.findByNameContainingIgnoreCaseOrNameArContainingIgnoreCase(name, name);
                if (brands.isEmpty()) {
                        throw new RuntimeException("لا يوجد براند بهذا الاسم");
                }
                return brands.stream()
                        .map(b -> new OutCarBrandDTO(b.getId(), b.getName(), b.getLogoPath()))
                        .toList();
        }

        // 🔤 فلترة بالحرف
        public List<OutCarBrandDTO> filterByLetter(String letter) {
                return carBrandRepository.findByNameArStartingWithIgnoreCase(letter)
                        .stream()
                        .map(this::toDto)
                        .toList();
        }

        private OutCarBrandDTO toDto(CarBrand brand) {
                return new OutCarBrandDTO(
                        brand.getId(),
                        brand.getNameAr(),     // نرجّع العربي
                        brand.getLogoPath()
                );
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
