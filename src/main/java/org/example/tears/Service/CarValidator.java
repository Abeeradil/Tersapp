package org.example.tears.Service;

import org.example.tears.Api.ApiException;
import org.example.tears.DTO.IstimaraData;
import org.example.tears.InpDTO.InpCarDto;
import org.example.tears.Model.*;
import org.example.tears.Repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CarValidator {

    // ================= MANUAL =================
    public void validateManual(InpCarDto dto, MultipartFile image) {

        if (dto == null) {
            throw new ApiException("DTO required");
        }

        if (image == null || image.isEmpty()) {
            throw new ApiException("Image required");
        }

        if (dto.getBrandId() == null) {
            throw new ApiException("Brand required");
        }

        if (dto.getModelId() == null) {
            throw new ApiException("Model required");
        }
    }

    // ================= UPDATE =================
    public void validateUpdate(InpCarDto dto) {
        if (dto == null) {
            throw new ApiException("DTO required");
        }
    }

    // ================= IMAGE =================
    public void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ApiException("Image required");
        }
    }

    // ================= OCR =================
    public void validateOcr(IstimaraData info) {

        if (info == null) {
            throw new ApiException("OCR empty result");
        }

        if (isBlank(info.getPlate_text_ar())
                && isBlank(info.getPlate_text_en())) {

            throw new ApiException("Plate not found");
        }

        if (isBlank(info.getVehicle_make())) {
            throw new ApiException("Vehicle make not found");
        }

        if (isBlank(info.getVehicle_model())) {
            throw new ApiException("Vehicle model not found");
        }
    }

    private boolean isBlank(String value) {

        return value == null
                || value.isBlank();
    }

    // ================= BRAND/MODEL =================
    public void validateBrandModel(CarBrand brand, CarModel model) {

        if (brand == null) {
            throw new ApiException("Brand not found");
        }

        if (model == null) {
            throw new ApiException("Model not found");
        }

        if (!model.getBrand().getId().equals(brand.getId())) {
            throw new ApiException("Model does not belong to brand");
        }
    }

    // ================= OWNERSHIP =================
    public Car validateOwnership(Integer carId, User user, CarRepository repo) {

        Car car = repo.findById(carId)
                .orElseThrow(() -> new ApiException("Car not found"));

        if (car.getCustomer() == null ||
                !car.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new ApiException("Not your car");
        }

        return car;
    }
}