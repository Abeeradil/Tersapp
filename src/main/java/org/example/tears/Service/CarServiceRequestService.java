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
import org.example.tears.OutDTO.PreviewResponseDto;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Model.*;
import org.example.tears.Repository.CarRepository;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    private final AppointmentService appointmentService;
    private final PricingCalculationService pricingCalculationService;
    private final RequestMapper requestMapper;

    private static final int HYDRAULIC_EXTRA = 100;
    private static final AtomicInteger ORDER_COUNTER = new AtomicInteger(1000);

    // ---------------------------
    // Step 1: Preview
    // ---------------------------
    public PreviewResponseDto preview(PreviewRequestDto dto) {

        int price = pricingCalculationService.calculatePreview(
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
    public RequestResponseDto createRequest(
            HttpServletRequest request,
            CreateRequestStepDto dto
    ) {

        // =========================
        // 1) Authenticated User
        // =========================
        User user = authService.getAuthenticatedUser(request);

        // =========================
        // 2) Validate Car Ownership
        // =========================
        boolean ownsCar = carRepository
                .findByCustomerId(user.getCustomer().getId())
                .stream()
                .anyMatch(car -> car.getId().equals(dto.getCarId()));

        if (!ownsCar) {
            throw new ApiException("السيارة المختارة لا تنتمي لهذا المستخدم");
        }

        // =========================
        // 3) Validate Problem Description
        // =========================
        if (dto.getProblemDescription() == null
                || dto.getProblemDescription().isBlank()) {

            throw new ApiException("وصف المشكلة إلزامي");
        }

        // =========================
        // 4) Service Option
        // =========================
        ServiceOption option;

        try {
            option = ServiceOption.valueOf(
                    dto.getServiceOption().toUpperCase()
            );
        } catch (Exception e) {
            throw new ApiException("نوع الخدمة غير صالح");
        }

        // =========================
        // 5) Resolve Location
        // =========================
        Location location = locationService.resolveLocation(dto, user);

        // =========================
        // 6) Validate Appointment
        // =========================
        appointmentService.validateAppointment(
                dto.getAppointmentDate(),
                dto.getAppointmentTime()
        );

        // =========================
        // 7) Calculate Price
        // =========================
        int estimatedPrice = pricingCalculationService.calculateFinal(
                dto.getServiceOption(),
                dto.isHydraulicTruck(),
                dto.getCouponCode()
        );

        // =========================
        // 8) Validate Payment Method
        // =========================
        if (dto.getPaymentMethod() == null
                || dto.getPaymentMethod().isBlank()) {

            throw new ApiException("طريقة الدفع مطلوبة");
        }

        PaymentMethod paymentMethod;

        try {
            paymentMethod = PaymentMethod.valueOf(
                    dto.getPaymentMethod().toUpperCase()
            );
        } catch (Exception e) {
            throw new ApiException("طريقة الدفع غير صالحة");
        }

        // =========================
        // 9) Create Request
        // =========================
        CarServiceRequest req = new CarServiceRequest();

        req.setCarId(dto.getCarId());

        req.setCustomer(user.getCustomer());

        req.setServiceOption(option);

        req.setProblemDescription(dto.getProblemDescription());

        req.setHydraulicTruck(dto.isHydraulicTruck());

        req.setAppointmentDate(dto.getAppointmentDate());

        req.setAppointmentTime(dto.getAppointmentTime());

        req.setEstimatedPrice(estimatedPrice);

        req.setInitialPaid(false);

        req.setFinalPaid(false);

        req.setPaymentMethod(paymentMethod);

        req.setLocation(location);

        req.setOrderNumber(
                "#" + ORDER_COUNTER.incrementAndGet()
        );

        req.setCustomerStatus(
                mapToCustomerStatus(WorkflowStage.PRICING)
        );

        req.setCreatedAt(LocalDateTime.now());

        // =========================
        // 10) Save
        // =========================
        CarServiceRequest saved = requestRepository.save(req);

        // =========================
        // 11) Response
        // =========================
        return toResponseDto(saved);
    }




    // ---------------------------
    // عرض طلبات المستخدم
    // ---------------------------
    public List<RequestResponseDto> getMyRequests(Integer userCustomerId) {
        return requestRepository.findByCustomerIdOrderByIdDesc(userCustomerId)
                .stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    private RequestResponseDto toResponseDto(CarServiceRequest r) {

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

        return dto;
    }

    private LocationDto mapLocation(Location loc) {

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


}