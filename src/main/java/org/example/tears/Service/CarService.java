package org.example.tears.Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.IstimaraData;
import org.example.tears.InpDTO.InpCarDto;
import org.example.tears.Model.*;
import org.example.tears.OutDTO.OutCarDetailsDTO;
import org.example.tears.OutDTO.OutMyCarDTO;
import org.example.tears.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CarService {


    // ================= DEPENDENCIES =================
    private final CarRepository carRepository;
    private final AuthService authService;
    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;
    private final SocketService socketService;


    private final OcrService ocrService;
    private final CarValidator carValidator;
    private final CarMapper carMapper;
    private final OpenAiIstimaraService openAiIstimaraService;

    // ================= MANUAL =================
    public Map<String, Object> registerCarManual(
            HttpServletRequest request,
            InpCarDto dto,
            MultipartFile image
    ) {

        User user = authService.getAuthenticatedUser(request);

        carValidator.validateManual(dto, image);

        CarBrand brand = carBrandRepository.findById(dto.getBrandId())
                .orElseThrow();

        CarModel model = carModelRepository.findById(dto.getModelId())
                .orElseThrow();

        carValidator.validateBrandModel(brand, model);

        Car car = carMapper.buildManualCar(dto, image, user, brand, model);

        carRepository.save(car);
        socketService.send(
                "/topic/cars/" + user.getId(),
                carMapper.toResponse(car, user.getFullName())
        );

        return carMapper.toResponse(car, user.getFullName());
    }

    // ================= AUTO =================
    public Map<String, Object> registerCarAuto(
            HttpServletRequest request,
            MultipartFile image,
            Integer mileage
    ) {

        User user = authService.getAuthenticatedUser(request);

        // 1. Validate image
        carValidator.validateImage(image);

        // 2. OpenAI OCR
        IstimaraData info =
                openAiIstimaraService.extractIstimara(image);

        // 3. Validate OCR
        carValidator.validateOcr(info);

        // 4. Detect brand
        CarBrand brand =
                carMapper.detectBrand(
                        info.getVehicle_make()
                );

        // 5. Detect model
        CarModel model =
                carMapper.detectModel(
                        info.getVehicle_model(),
                        brand
                );

        // 6. Validate brand/model
        carValidator.validateBrandModel(
                brand,
                model
        );

        // 7. Build car
        Car car =
                carMapper.buildAutoCar(
                        info,
                        image,
                        user,
                        brand,
                        model,
                        mileage
                );

        // 8. Save
        carRepository.save(car);

        // 9. Build response
        Map<String, Object> response =
                carMapper.toResponse(
                        car,
                        user.getFullName()
                );

        // OCR owner name
        response.put(
                "istimaraOwnerName",
                info.getOwner_name()
        );

        // 10. Send realtime update
        socketService.send(
                "/topic/cars/" + user.getId(),
                response
        );

        return response;
    }

    // ================= GET =================
    public List<OutMyCarDTO> getMyCars(HttpServletRequest request) {

        User user = authService.getAuthenticatedUser(request);

        return carRepository.findByCustomerId(user.getCustomer().getId())
                .stream()
                .map(carMapper::toDto)
                .toList();
    }

    // ================= UPDATE =================
    public Map<String, Object> updateCar(
            HttpServletRequest request,
            Integer carId,
            InpCarDto dto
    ) {

        User user = authService.getAuthenticatedUser(request);

        Car car = carValidator.validateOwnership(carId, user, carRepository);

        carValidator.validateUpdate(dto);

        carMapper.updateCar(car, dto, carBrandRepository, carModelRepository);

        carRepository.save(car);

        Map<String, Object> response =
                carMapper.toResponse(
                        car,
                        user.getFullName()
                );

        socketService.send(
                "/topic/cars/" + user.getId(),
                response
        );

        return response;
    }

    // ================= DELETE =================
    public void deleteCar(HttpServletRequest request, Integer carId) {

        User user = authService.getAuthenticatedUser(request);

        Car car = carValidator.validateOwnership(carId, user, carRepository);

        carRepository.delete(car);
    }

    // ================= CAR DETAILS =================
    public OutCarDetailsDTO getCarDetails(
            HttpServletRequest request,
            Integer carId
    ) {

        User user = authService.getAuthenticatedUser(request);

        Car car = carValidator.validateOwnership(
                carId,
                user,
                carRepository
        );


        return carMapper.toCarDetailsDto(
                car,
                user.getFullName()
        );
    }
    private boolean isOwnerMatched(
            String formOwnerName,
            String userName
    ) {

        if (formOwnerName == null || formOwnerName.isBlank()) {
            return false;
        }

        if (userName == null || userName.isBlank()) {
            return false;
        }

        String formName = normalizeName(formOwnerName);
        String accountName = normalizeName(userName);

        return formName.equals(accountName);
    }

    private String normalizeName(String name) {

        return name
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }


}