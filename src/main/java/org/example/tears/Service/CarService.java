package org.example.tears.Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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

        carValidator.validateImage(image);

        Map<String, String> info = ocrService.extractCarInfo(image);

        carValidator.validateOcr(info, user);

        CarBrand brand = carMapper.detectBrand(info.get("brandName"));
        CarModel model = carMapper.detectModel(info.get("modelName"), brand);

        Car car = carMapper.buildAutoCar(info, user, brand, model, mileage);

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


}