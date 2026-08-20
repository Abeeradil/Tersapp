package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.IstimaraData;
import org.example.tears.InpDTO.InpCarDto;
import org.example.tears.Model.Car;
import org.example.tears.Model.CarModel;
import org.example.tears.Model.CarBrand;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.OutCarDetailsDTO;
import org.example.tears.OutDTO.OutMyCarDTO;
import org.example.tears.Repository.CarBrandRepository;
import org.example.tears.Repository.CarModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CarMapper {

    private final PlateService plateService;
    private final FileStorageService fileStorageService;


    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;


    // ================= MANUAL BUILD =================
    public Car buildManualCar(
            InpCarDto dto,
            MultipartFile image,
            User user,
            CarBrand brand,
            CarModel model
    ) {

        Car car = new Car();

        car.setCarYear(dto.getCarYear());
        car.setMileage(dto.getMileage());

        car.setBrand(brand);
        car.setModel(model);

        car.setCustomer(user.getCustomer());

        if (image != null && !image.isEmpty()) {
            car.setFormImagePath(fileStorageService.saveFile(image, "forms"));
        }

        String ar = dto.getPlateNumberArabic();
        String en = dto.getPlateNumberEnglish();

        if (ar != null) {
            ar = plateService.normalizePlate(ar);
            en = plateService.convertPlateToEnglish(ar);
        }

        car.setPlateNumberArabic(ar);
        car.setPlateNumberEnglish(en);

        return car;
    }

    // ================= AUTO BUILD =================
    public Car buildAutoCar(
            IstimaraData info,
            MultipartFile image,
            User user,
            CarBrand brand,
            CarModel model,
            Integer mileage
    ) {

        Car car = new Car();

        car.setCustomer(user.getCustomer());
        car.setBrand(brand);
        car.setModel(model);
        car.setMileage(mileage);

        // ================= PLATE =================

        String lettersAr = info.getPlate_text_ar();
        String lettersEn = info.getPlate_text_en();
        String number = info.getPlate_number();

        String ar = plateService.buildArabicPlate(
                lettersAr,
                number
        );

        String en = null;

        if (lettersEn != null && !lettersEn.isBlank()
                && number != null && !number.isBlank()) {

            en = number.trim() + " " + lettersEn.trim();

        } else if (lettersEn != null && !lettersEn.isBlank()) {

            en = lettersEn.trim();

        } else if (number != null && !number.isBlank()) {

            en = number.trim();
        }

        car.setPlateNumberArabic(ar);
        car.setPlateNumberEnglish(en);

        // ================= YEAR =================

        car.setCarYear(
                parseYear(info.getModel_year())
        );

        // ================= IMAGE =================

        if (image != null && !image.isEmpty()) {

            car.setFormImagePath(
                    fileStorageService.saveFile(
                            image,
                            "forms"
                    )
            );
        }

        return car;
    }

    // ================= UPDATE =================
    public void updateCar(
            Car car,
            InpCarDto dto,
            CarBrandRepository brandRepo,
            CarModelRepository modelRepo
    ) {

        if (dto.getCarYear() != null) {
            car.setCarYear(dto.getCarYear());
        }

        if (dto.getMileage() != null) {
            car.setMileage(dto.getMileage());
        }

        if (dto.getBrandId() != null) {
            CarBrand brand = brandRepo.findById(dto.getBrandId()).orElseThrow();
            car.setBrand(brand);
        }

        if (dto.getModelId() != null) {
            CarModel model = modelRepo.findById(dto.getModelId()).orElseThrow();
            car.setModel(model);
        }

        if (dto.getPlateNumberArabic() != null) {
            String ar = plateService.normalizePlate(dto.getPlateNumberArabic());
            car.setPlateNumberArabic(ar);
            car.setPlateNumberEnglish(plateService.convertPlateToEnglish(ar));
        }
    }

    // ================= RESPONSE =================
    public Map<String, Object> toResponse(Car car, String owner) {

        Map<String, Object> m = new LinkedHashMap<>();

        m.put("carId", car.getId());
        m.put("owner", owner);

        m.put("brand", car.getBrand().getName());
        m.put("model", car.getModel().getName());

        m.put("plateAr", car.getPlateNumberArabic());
        m.put("plateEn", car.getPlateNumberEnglish());

        m.put("year", car.getCarYear());
        m.put("mileage", car.getMileage());

        return m;
    }

    // ================= DTO =================
    public OutMyCarDTO toDto(Car car) {

        OutMyCarDTO dto = new OutMyCarDTO();

        dto.setCarId(car.getId());
        dto.setPlateNumberArabic(car.getPlateNumberArabic());
        dto.setBrandNameAr(car.getBrand().getNameAr());
        dto.setModelNameAr(car.getModel().getNameAr());
        dto.setCarYear(car.getCarYear());

        return dto;
    }

    // ================= DETECT BRAND =================
    public CarBrand detectBrand(String text) {

        return carBrandRepository.findAll().stream()
                .filter(b ->
                        text != null &&
                                (text.contains(b.getName()) ||
                                        text.contains(b.getNameAr())))
                .findFirst()
                .orElse(null);
    }

    // ================= DETECT MODEL =================
    public CarModel detectModel(String text, CarBrand brand) {

        if (brand == null) return null;

        return carModelRepository.findByBrandId(brand.getId())
                .stream()
                .filter(m ->
                        text != null &&
                                (text.contains(m.getName()) ||
                                        text.contains(m.getNameAr())))
                .findFirst()
                .orElse(null);
    }

    // ================= CAR DETAILS DTO =================
    public OutCarDetailsDTO toCarDetailsDto(Car car, String ownerName) {

        OutCarDetailsDTO dto = new OutCarDetailsDTO();

        dto.setCarId(car.getId());

        dto.setOwnerName(ownerName);

        dto.setBrandName(car.getBrand().getName());
        dto.setBrandNameAr(car.getBrand().getNameAr());

        dto.setModelName(car.getModel().getName());
        dto.setModelNameAr(car.getModel().getNameAr());

        dto.setPlateNumberArabic(car.getPlateNumberArabic());
        dto.setPlateNumberEnglish(car.getPlateNumberEnglish());

        dto.setCarYear(car.getCarYear());
        dto.setMileage(car.getMileage());

        dto.setFormImagePath(car.getFormImagePath());

        return dto;
    }

    private Integer parseYear(String y) {
        try {
            return y == null ? null : Integer.parseInt(y);
        } catch (Exception e) {
            return null;
        }
    }
}