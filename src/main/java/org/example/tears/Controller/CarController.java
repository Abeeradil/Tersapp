package org.example.tears.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.OutDTO.OutCarBrandDTO;
import org.example.tears.OutDTO.OutCarModelDTO;
import org.example.tears.Model.Car;
import org.example.tears.InpDTO.InpCarDto;
import org.example.tears.OutDTO.OutMyCarDTO;
import org.example.tears.Service.CarBrandService;
import org.example.tears.Service.CarModelService;
import org.example.tears.Service.CarService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/tears/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final CarBrandService carBrandService;
    private final CarModelService carModelService;

    // 🔹 GET all brands
    @GetMapping("/brands")
    public ResponseEntity<List<OutCarBrandDTO>> getBrands() {
        return ResponseEntity.ok(carBrandService.getAllBrands());
    }

    @GetMapping("/brands/{brandId}/models")
    public ResponseEntity<List<OutCarModelDTO>> getModels(@PathVariable Integer brandId) {
        return ResponseEntity.ok(carModelService.getModelsByBrand(brandId));
    }


    @GetMapping("/brands/search")
    public ResponseEntity<List<OutCarBrandDTO>> searchBrands(

            @RequestParam(required = false) String keyword,

            @RequestParam(defaultValue = "asc") String sort
    ) {

        return ResponseEntity.ok(
                carBrandService.searchBrands(keyword, sort)
        );
    }

    @GetMapping("/models/search")
    public ResponseEntity<List<OutCarModelDTO>> searchModels(

            @RequestParam Integer brandId,

            @RequestParam(required = false) String keyword,

            @RequestParam(defaultValue = "asc") String sort
    ) {

        return ResponseEntity.ok(
                carModelService.searchModels(
                        brandId,
                        keyword,
                        sort
                )
        );
    }
    /*--------------------------------------------------------------
      🧩 1) التسجيل التلقائي من الاستمارة (OCR)
     --------------------------------------------------------------*/
    @PostMapping("/register/auto")
    public ResponseEntity<Map<String, Object>> registerCarAuto(
            HttpServletRequest request,
            @RequestParam(value = "formImage", required = false) MultipartFile formImage ,
            @RequestParam(value = "mileage", required = false) Integer mileage
    ) {
        Map<String, Object> response = carService.registerCarAuto(request, formImage, mileage);
        return ResponseEntity.ok(response);
    }


    /*--------------------------------------------------------------
     🧩 2) التسجيل اليدوي
    --------------------------------------------------------------*/
    @PostMapping(
            value = "/register/manual",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> registerCarManual(

            HttpServletRequest request,

            @RequestParam Integer carYear,
            @RequestParam String plateNumberArabic,
            @RequestParam(required = false) String plateNumberEnglish,
            @RequestParam Integer mileage,
            @RequestParam Integer brandId,
            @RequestParam Integer modelId,

            @RequestParam(value = "formImage", required = false)
            MultipartFile formImage
    ) {

        InpCarDto dto = new InpCarDto();

        dto.setCarYear(carYear);
        dto.setPlateNumberArabic(plateNumberArabic);
        dto.setPlateNumberEnglish(plateNumberEnglish);
        dto.setMileage(mileage);
        dto.setBrandId(brandId);
        dto.setModelId(modelId);

        return ResponseEntity.ok(
                carService.registerCarManual(request, dto, formImage)
        );
    }

    /*--------------------------------------------------------------
     🧩 3) التسجيل بالتفويض (استمارة + تفويض)
    --------------------------------------------------------------*/
//    @PostMapping("/register/authorization")
//    public ResponseEntity<Map<String, String>> registerCarByAuthorization(
//            HttpServletRequest request,
//            @RequestParam("formImage") MultipartFile formImage,
//            @RequestParam("authorizationDoc") MultipartFile authorizationDoc
//    ) {
//        Map<String, String> response = carService.registerCarByAuthorization(request, formImage, authorizationDoc);
//        return ResponseEntity.ok(response);
//    }

//    @PostMapping("/extract-user-name")
//    public ResponseEntity<Map<String, String>> testExtractUserName(
//            @RequestParam MultipartFile authorizationDoc) {
//
//        try {
//            Map<String, String> response = carService.extractUserNameFromAuthorization(authorizationDoc);
//            return ResponseEntity.ok(response);
//
//        } catch (RuntimeException e) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }

    // جلب كل سيارات المستخدم
    @GetMapping("/my-car")
    public ResponseEntity<List<OutMyCarDTO>> getUserCars(HttpServletRequest request) {
        List<OutMyCarDTO> cars = carService.getMyCars(request);
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/search-model")
    public List<OutCarModelDTO> searchForModel(
            @RequestParam Integer brandId,
            @RequestParam String nameAr,
            @RequestParam String nameEn ) {
        return carModelService.searchModels(brandId, nameAr,nameEn);
    }

    @PostMapping("/extract-owner")
    public ResponseEntity<?> extractOwner(
            @RequestParam MultipartFile formImage) {

        return ResponseEntity.ok(carService.extractOwnerName(formImage));
    }

}