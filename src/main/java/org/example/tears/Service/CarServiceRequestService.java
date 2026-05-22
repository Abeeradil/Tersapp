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
    public RequestResponseDto createRequest(HttpServletRequest request, CreateRequestStepDto dto) {

        // 1️⃣ المستخدم
        User user = authService.getAuthenticatedUser(request);

        // 2️⃣ تحقق السيارة
        boolean owns = carRepository.findByCustomerId(user.getCustomer().getId())
                .stream().anyMatch(c -> c.getId().equals(dto.getCarId()));
        if (!owns)
            throw new ApiException("السيارة المختارة لا تنتمي لهذا المستخدم");

        // 3️⃣ وصف المشكلة
        if (dto.getProblemDescription() == null || dto.getProblemDescription().isBlank())
            throw new RuntimeException("وصف المشكلة إلزامي");

        ServiceOption option = ServiceOption.valueOf(dto.getServiceOption());

        Location location = locationService.resolveLocation(dto, user);

        LocalDate date = LocalDate.parse(dto.getAppointmentDate());
        appointmentService.getAvailability(date);


        int estimatedPrice = pricingCalculationService.calculateFinal(
                dto.getServiceOption(),
                dto.isHydraulicTruck(),
                dto.getCouponCode()
        );


        // 7️⃣ التحقق من طريقة الدفع
        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().isBlank())
            throw new RuntimeException("طريقة الدفع مطلوبة");

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("طريقة الدفع غير صالحة");
        }

        // 8️⃣ إنشاء الطلب
        CarServiceRequest req = new CarServiceRequest();
        req.setCarId(dto.getCarId());
        req.setCustomer(user.getCustomer());
        req.setServiceOption(option);
        req.setHydraulicTruck(dto.isHydraulicTruck());
        req.setProblemDescription(dto.getProblemDescription());
        req.setAppointmentDate(dto.getAppointmentDate());
        req.setAppointmentTime(dto.getAppointmentTime());
        req.setEstimatedPrice(estimatedPrice);

        // الدفع الجزئي للدفعة الأولى
        req.setInitialPaid(false); // يمكن تغييره لاحقًا إذا الدفع تم
        req.setInitialTransactionId(null);

        req.setOrderNumber("#" + ORDER_COUNTER.incrementAndGet());
        req.setCustomerStatus(mapToCustomerStatus(WorkflowStage.PRICING));
        req.setPaymentMethod(method);
        req.setLocation(location);
        req.setCreatedAt(LocalDateTime.now());

        CarServiceRequest saved = requestRepository.save(req);

        // 9️⃣ Response
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

        // حالة الطلب للعميل مع فحص null
        dto.setStatus(r.getCustomerStatus() != null ? r.getCustomerStatus().name() : "REQUEST_CREATED");

        // السعر التقديري (الدفع الأول) مع فحص null
        dto.setTotalPrice(r.getEstimatedPrice() != null ? r.getEstimatedPrice() : 0);

        dto.setAppointmentDate(r.getAppointmentDate());
        dto.setAppointmentTime(r.getAppointmentTime());
        dto.setPaymentMethod(r.getPaymentMethod() != null ? r.getPaymentMethod().name() : "UNKNOWN");

        Location loc = r.getLocation();
        if (loc != null) {
            dto.setLocationId(loc.getId());
            dto.setLat(loc.getLat());
            dto.setLng(loc.getLng());
            dto.setAddress(loc.getAddress());
        }

        dto.setHydraulicTruck(r.isHydraulicTruck());

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