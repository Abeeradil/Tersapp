package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.RequestMapper;
import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.PaymentMethod;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.Enums.WorkflowStage;
import org.example.tears.InpDTO.LocationDto;
import org.example.tears.InpDTO.PreviewRequestDto;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.InpDTO.UpdateRequestDto;
import org.example.tears.OutDTO.PreviewResponseDto;
import org.example.tears.OutDTO.PricingResponse;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Model.*;
import org.example.tears.Repository.CarRepository;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.CouponRepository;
import org.example.tears.Repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceRequestService {

    private final CarServiceRequestRepository requestRepository;
    private final CarRepository carRepository;
    private final AuthService authService;
    private final CouponRepository couponRepository;
    private final LocationService locationService;
    private final LocationRepository locationRepository;
    private final AppointmentService appointmentService;
    private final PricingCalculationService pricingCalculationService;
    private final RequestMapper requestMapper;

    private static final int HYDRAULIC_EXTRA = 100;
    private static final AtomicInteger ORDER_COUNTER = new AtomicInteger(1000);

    // ---------------------------
    // Step 1: Preview
    // ---------------------------
    public PreviewResponseDto preview(PreviewRequestDto dto) {

        double price = pricingCalculationService.calculatePreview(
                dto.getServiceOption(),
                dto.isHydraulicTruck()
        );

        PreviewResponseDto resp = new PreviewResponseDto();
        resp.setEstimatedPrice(price);

        ServiceOption option = ServiceOption.valueOf(dto.getServiceOption());
        resp.setDetails("خدمة: " + option.getDisplayName() + " — سعر تقديري: " + price + " ريال");

        return resp;
    }

    // ---------------------------
    // Step 2: Create Final Request
    // ---------------------------
    @Transactional
    public RequestResponseDto createRequest(HttpServletRequest request, CreateRequestStepDto dto) {
        User user = authService.getAuthenticatedUser(request);

        CarServiceRequest req = buildValidatedRequest(user, dto);

        CarServiceRequest saved = requestRepository.save(req);

        return toResponseDto(saved);
    }

    public RequestResponseDto updateRequest(
            HttpServletRequest request,
            Integer requestId,
            UpdateRequestDto dto
    ) {
        User user = authService.getAuthenticatedUser(request);

        CarServiceRequest serviceRequest = requestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new ApiException("الطلب غير موجود")
                );
        // =========================
// Car
// =========================
        if (dto.getCarId() != null) {

            boolean ownsCar = carRepository
                    .findByCustomerId(user.getCustomer().getId())
                    .stream()
                    .anyMatch(car ->
                            car.getId().equals(dto.getCarId())
                    );

            if (!ownsCar) {
                throw new ApiException(
                        "السيارة المختارة لا تنتمي لهذا المستخدم"
                );
            }

            serviceRequest.setCarId(dto.getCarId());
        }

// ownership check
        if (!serviceRequest.getCustomer().getId()
                .equals(user.getCustomer().getId())) {

            throw new ApiException("هذا الطلب لا يخص المستخدم");
        }

// prevent edit after payment
        if (serviceRequest.isInitialPaid()) {
            throw new ApiException("لا يمكن تعديل الطلب بعد الدفع");
        }

// =========================
// Problem Description
// =========================
        if (dto.getProblemDescription() != null) {

            serviceRequest.setProblemDescription(
                    dto.getProblemDescription()
            );
        }

// =========================
// Hydraulic Truck
// =========================
        if (dto.getHydraulicTruck() != null) {

            serviceRequest.setHydraulicTruck(
                    dto.getHydraulicTruck()
            );
        }

// =========================
// Appointment
// =========================
        if (dto.getAppointmentDate() != null
                && dto.getAppointmentTime() != null) {

            appointmentService.validateAppointment(
                    dto.getAppointmentDate(),
                    dto.getAppointmentTime()
            );

            serviceRequest.setAppointmentDate(
                    dto.getAppointmentDate()
            );

            serviceRequest.setAppointmentTime(
                    dto.getAppointmentTime()
            );
        }

// =========================
// Service Option
// =========================
        if (dto.getServiceOption() != null) {

            ServiceOption option = ServiceOption.valueOf(
                    dto.getServiceOption().toUpperCase()
            );

            serviceRequest.setServiceOption(option);
        }
        if (serviceRequest.getCustomerStatus()
                == CustomerRequestStatus.CANCELED) {

            throw new ApiException("لا يمكن تعديل طلب ملغي");
        }

// =========================
// Payment Method
// =========================
        if (dto.getPaymentMethod() != null) {

            PaymentMethod method = PaymentMethod.valueOf(
                    dto.getPaymentMethod().toUpperCase()
            );

            serviceRequest.setPaymentMethod(method);
        }

// =========================
// Location
// =========================
        if (dto.getLocationId() != null) {

            Location location = locationRepository
                    .findById(dto.getLocationId())
                    .orElseThrow(() ->
                            new ApiException("الموقع غير موجود")
                    );

            serviceRequest.setLocation(location);
        }
// =========================
// Recalculate Price
// =========================
        PricingResponse pricing = pricingCalculationService.calculateFinal(
                serviceRequest.getServiceOption().name(),
                serviceRequest.isHydraulicTruck(),
                dto.getCouponCode()
        );

        serviceRequest.setEstimatedPrice(pricing.finalPrice);
        serviceRequest.setOriginalPrice(pricing.originalPrice);
        serviceRequest.setDiscount(pricing.discount);
        serviceRequest.setVatAmount(pricing.vatAmount);
        serviceRequest.setCouponValid(pricing.couponValid);
        serviceRequest.setPricingMessage(pricing.message);

        serviceRequest.setLastUpdated(LocalDateTime.now());

        CarServiceRequest updated = requestRepository.save(serviceRequest);

        return toResponseDto(updated);
    }

    public List<RequestResponseDto> getCurrentRequests(Integer customerId) {

        return requestRepository
                .findByCustomerIdAndCustomerStatusNotInOrderByIdDesc(
                        customerId,
                        List.of(
                                CustomerRequestStatus.DELIVERED,
                                CustomerRequestStatus.CANCELED
                        )
                )
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<RequestResponseDto> getPastRequests(Integer customerId) {

        return requestRepository
                .findByCustomerIdAndCustomerStatusInOrderByIdDesc(
                        customerId,
                        List.of(
                                CustomerRequestStatus.DELIVERED,
                                CustomerRequestStatus.CANCELED
                        )
                )
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }



    // ---------------------------
    // عرض طلبات المستخدم
    // ---------------------------
    public List<RequestResponseDto> getMyRequests(Integer userCustomerId) {
        return requestRepository.findByCustomerIdOrderByIdDesc(userCustomerId)
                .stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    public RequestResponseDto toResponseDto(CarServiceRequest r) {

        RequestResponseDto dto = new RequestResponseDto();

        dto.setId(r.getId());
        dto.setOrderNumber(r.getOrderNumber());

        dto.setStatus(
                r.getCustomerStatus() != null
                        ? r.getCustomerStatus().name()
                        : "REQUEST_CREATED"
        );

        dto.setTotalPrice(
                r.getEstimatedPrice() != null
                        ? r.getEstimatedPrice()
                        : 0
        );

        dto.setAppointmentDate(r.getAppointmentDate());
        dto.setAppointmentTime(r.getAppointmentTime());

        dto.setHydraulicTruck(r.isHydraulicTruck());

        dto.setPaymentMethod(
                r.getPaymentMethod() != null
                        ? r.getPaymentMethod().name()
                        : null
        );

        dto.setLocation(mapLocation(r.getLocation()));
        dto.setTotalPrice(
                r.getEstimatedPrice()
        );

        dto.setOriginalPrice(
                r.getOriginalPrice()
        );

        dto.setDiscount(
                r.getDiscount()
        );

        dto.setVatAmount(
                r.getVatAmount()
        );

        dto.setCouponValid(
                r.getCouponValid()
        );

        dto.setPricingMessage(
                r.getPricingMessage()
        );

        // 🚗 هنا أهم جزء: نجيب السيارة
        Car car = carRepository.findById(r.getCarId())
                .orElse(null);

        if (car != null) {
            dto.setPlateNumberArabic(car.getPlateNumberArabic());
            dto.setPlateNumberEnglish(car.getPlateNumberEnglish());
        }

        return dto;
    }

    public LocationDto mapLocation(Location loc) {

        if (loc == null) {
            return null;
        }

        LocationDto dto = new LocationDto();

        dto.setId(loc.getId());

        dto.setLat(loc.getLat());

        dto.setLng(loc.getLng());

        dto.setAddress(loc.getAddress());

        dto.setTitle(loc.getTitle());

        return dto;
    }


    private CustomerRequestStatus mapToCustomerStatus(WorkflowStage stage) {

        return switch (stage) {

            case NEW_REQUEST ->
                    CustomerRequestStatus.REQUEST_CREATED;

            case RECEIVED ->
                    CustomerRequestStatus.CAR_RECEIVED;

            case INSPECTION, PARTS_REGISTERED, PRICING, WAITING_APPROVAL, REPAIRING ->
                    CustomerRequestStatus.CAR_INSPECTION;


            case READY ->
                    CustomerRequestStatus.READY_FOR_DELIVERY;

            case DELIVERED ->
                    CustomerRequestStatus.DELIVERED;

            case CANCELLED ->
                    CustomerRequestStatus.CANCELED;
        };
    }

    public CarServiceRequest buildValidatedRequest(User user, CreateRequestStepDto dto) {
        boolean ownsCar = carRepository.findByCustomerId(user.getCustomer().getId())
                .stream()
                .anyMatch(c -> c.getId().equals(dto.getCarId()));

        if (!ownsCar) {
            throw new ApiException("السيارة لا تنتمي للمستخدم");
        }

        if (dto.getProblemDescription() == null || dto.getProblemDescription().isBlank()) {
            throw new ApiException("وصف المشكلة إلزامي");
        }

        ServiceOption option = ServiceOption.valueOf(dto.getServiceOption().toUpperCase());

        Location location = locationService.resolveLocation(dto, user);

        appointmentService.validateAppointment(
                dto.getAppointmentDate(),
                dto.getAppointmentTime()
        );

        PricingResponse pricing = pricingCalculationService.calculateFinal(
                dto.getServiceOption(),
                dto.isHydraulicTruck(),
                dto.getCouponCode()
        );

        PaymentMethod method = PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());

        CarServiceRequest req = new CarServiceRequest();

        req.setCarId(dto.getCarId());
        req.setCustomer(user.getCustomer());
        req.setServiceOption(option);
        req.setProblemDescription(dto.getProblemDescription());
        req.setHydraulicTruck(dto.isHydraulicTruck());
        req.setAppointmentDate(dto.getAppointmentDate());
        req.setAppointmentTime(dto.getAppointmentTime());
        req.setEstimatedPrice(
                pricing.finalPrice
        );

        req.setOriginalPrice(
                pricing.originalPrice
        );

        req.setDiscount(
                pricing.discount
        );

        req.setVatAmount(
                pricing.vatAmount
        );

        req.setCouponValid(
                pricing.couponValid
        );

        req.setPricingMessage(
                pricing.message
        );
        req.setPaymentMethod(method);
        req.setLocation(location);
        req.setOrderNumber("#" + UUID.randomUUID().toString().substring(0, 8));
        req.setCustomerStatus(mapToCustomerStatus(WorkflowStage.PRICING));
        req.setCreatedAt(LocalDateTime.now());

        return req;
    }

}