package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.*;
import org.example.tears.Enums.*;
import org.example.tears.InpDTO.LocationDto;
import org.example.tears.InpDTO.PreviewRequestDto;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.InpDTO.UpdateRequestDto;
import org.example.tears.OutDTO.PreviewResponseDto;
import org.example.tears.OutDTO.PricingResponse;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Model.*;
import org.example.tears.Repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceRequestService {

    private final CarServiceRequestRepository requestRepository;
    private final CarRepository carRepository;
    private final AuthService authService;
    private final LocationService locationService;
    private final LocationRepository locationRepository;
    private final AppointmentService appointmentService;
    private final RequestImageRepository imageRepo;
    private final PricingCalculationService pricingCalculationService;
    private final RequestReviewRepository reviewRepository;
    private final WarrantyRepository warrantyRequestRepository;
    private final SocketService socketService;



    // ---------------------------
    // Step 1: Preview
    // ---------------------------
    public PreviewResponseDto preview(PreviewRequestDto dto) {

        ServiceOption option =
                ServiceOption.valueOf(dto.getServiceOption());

        double servicePrice = option.getPrice() * 1.15;

        double hydraulicPrice =
                option == ServiceOption.ELECTRONIC_CHECK
                        ? 0
                        : 150 * 1.15;

        PreviewResponseDto resp = new PreviewResponseDto();

        resp.setServicePrice(servicePrice);

        resp.setHydraulicTruckPrice(hydraulicPrice);

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

        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ApiException("السيارة غير موجودة"));

        serviceRequest.setCar(car);

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

    public List<String> getRequestImages(Integer requestId, Integer customerId) {

        CarServiceRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("غير موجود"));

        if (!req.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("غير مصرح");
        }

        return imageRepo.findByRequestIdAndVisibleToCustomerTrue(requestId)
                .stream()
                .map(RequestImage::getImageUrl)
                .toList();
    }

    public List<CurrentRequestDto> getCurrentRequests(Integer customerId) {

        return requestRepository
                .findByCustomerIdAndCustomerStatusNotInOrderByCreatedAtDesc(
                        customerId,
                        List.of(
                                CustomerRequestStatus.DELIVERED,
                                CustomerRequestStatus.CANCELED
                        )
                )
                .stream()
                .map(this::toCurrentDto)
                .toList();
    }

    public List<RequestHistoryDto> getPastRequests(Integer customerId) {

        List<CarServiceRequest> requests =
                requestRepository
                        .findByCustomerIdAndCustomerStatusInOrderByCreatedAtDesc(
                                customerId,
                                List.of(
                                        CustomerRequestStatus.DELIVERED,
                                        CustomerRequestStatus.CANCELED
                                )
                        );

        Set<Integer> warrantyIds =
                warrantyRequestRepository.findByCustomer_Id(customerId)
                        .stream()
                        .map(w -> w.getRequest().getId())
                        .collect(Collectors.toSet());

        requests.sort(
                Comparator
                        .comparing((CarServiceRequest r) -> !warrantyIds.contains(r.getId()))
                        .thenComparing(CarServiceRequest::getCreatedAt, Comparator.reverseOrder())
        );

        return requests.stream()
                .map(this::toHistoryDto)
                .toList();
    }



    // ---------------------------
    // عرض طلبات المستخدم
    // ---------------------------
    public List<RequestResponseDto> getMyRequests(Integer userCustomerId) {
        return requestRepository.findByCustomerIdOrderByIdDesc(userCustomerId)
                .stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    public CurrentRequestDto toCurrentDto(CarServiceRequest req) {

        CurrentRequestDto dto = new CurrentRequestDto();

        dto.setId(req.getId());
        dto.setOrderNumber(req.getOrderNumber());

        if (req.getServiceOption() != null) {
            dto.setServiceName(req.getServiceOption().name());
        }

        if (req.getCustomerStatus() != null) {
            dto.setStatus(req.getCustomerStatus().name());
        }

        dto.setRequestState(
                mapRequestState(req)
        );

        return dto;
    }

    public RequestHistoryDto toHistoryDto(CarServiceRequest req) {

        RequestHistoryDto dto = new RequestHistoryDto();

        Optional<WarrantyRequest> warranty =
                warrantyRequestRepository.findByRequestId(req.getId());

        dto.setWarrantyRequest(warranty.isPresent());

        dto.setWarrantyStatus(
                warranty.map(w -> w.getStatus().name()).orElse(null)
        );
        dto.setId(req.getId());
        dto.setOrderNumber(req.getOrderNumber());

        if (req.getServiceOption() != null) {
            dto.setServiceName(req.getServiceOption().name());
        }

        dto.setAppointmentDate(req.getAppointmentDate());
        dto.setAppointmentTime(req.getAppointmentTime());

        dto.setTotalPrice(
                req.getFinalPrice() != null
                        ? req.getFinalPrice().doubleValue()
                        : req.getEstimatedPrice()
        );

        boolean reviewed =
                reviewRepository.existsByRequestId(req.getId());

        dto.setReviewed(reviewed);

        dto.setCanReview(
                req.getCustomerStatus() == CustomerRequestStatus.DELIVERED
                        && !reviewed
        );

        dto.setRequestState(
                mapRequestState(req)
        );

        if (req.getCustomerStatus() != null) {
            dto.setCustomerStatus(req.getCustomerStatus().name());
        }

        return dto;
    }

    public RequestResponseDto toResponseDto(CarServiceRequest r) {

        RequestResponseDto dto = new RequestResponseDto();

        dto.setId(r.getId());
        dto.setOrderNumber(r.getOrderNumber());


        boolean warrantyRequestExists =
                warrantyRequestRepository.existsByRequestId(r.getId());

        dto.setWarrantyRequest(warrantyRequestExists);


        dto.setStatus(
                r.getCustomerStatus() != null
                        ? r.getCustomerStatus().name()
                        : "REQUEST_CREATED"
        );

        WarrantyStatus warrantyStatus = null;

        Optional<WarrantyRequest> warranty =
                warrantyRequestRepository.findByRequestId(r.getId());

        if (warranty.isPresent()) {
            warrantyStatus = warranty.get().getStatus();
        }

        dto.setWarrantyStatus(
                warrantyStatus != null ? warrantyStatus.name() : null
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
        dto.setAmountPaid(r.getInitialPaymentAmount());

        dto.setAmountPaidHalalah(r.getInitialPaymentAmountHalalah());

        dto.setInitialPaymentMethod(
                r.getInitialPaymentMethod() != null
                        ? r.getInitialPaymentMethod().name()
                        : null
        );

        dto.setInitialPaymentStatus(
                r.getInitialPaymentStatus() != null
                        ? r.getInitialPaymentStatus().name()
                        : null
        );

        dto.setRemainingAmount(r.getRemainingAmount());

        dto.setNextPaymentMethod(
                r.getNextPaymentMethod() != null
                        ? r.getNextPaymentMethod().name()
                        : null
        );

        dto.setNextPaymentStatus(
                r.getNextPaymentStatus() != null
                        ? r.getNextPaymentStatus().name()
                        : null
        );

        // 🚗 هنا أهم جزء: نجيب السيارة
        Car car = carRepository.findById(r.getCar().getId())
                .orElse(null);

        if (car != null) {
            dto.setPlateNumberArabic(
                    formatArabicPlate(r.getCar().getPlateNumberArabic())
            );

            dto.setPlateNumberEnglish(
                    formatEnglishPlate(r.getCar().getPlateNumberEnglish())
            );
        }

        return dto;
    }

    private String formatEnglishPlate(String plate) {

        if (plate == null || plate.length() < 4) {
            return plate;
        }

        String letters = plate.substring(0, 3);
        String numbers = plate.substring(3);

        return letters + "-" + numbers;
    }

    private String formatArabicPlate(String plate) {

        if (plate == null || plate.isBlank()) {
            return plate;
        }

        String[] parts = plate.trim().split("\\s+");

        if (parts.length == 4) {
            return parts[0] + " " + parts[1] + " " + parts[2] + " - " + parts[3];
        }

        return plate;
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

    public RequestDetailsDto getRequestDetails(
            Integer customerId,
            Integer requestId
    ) {

        CarServiceRequest req = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ApiException("الطلب غير موجود")
                );

        if (!req.getCustomer().getId().equals(customerId)) {
            throw new ApiException("غير مصرح");
        }

        return toDetailsDto(req);
    }

    public RequestDetailsDto toDetailsDto(
            CarServiceRequest req
    ) {

        RequestDetailsDto dto =
                new RequestDetailsDto();

        dto.setId(req.getId());

        dto.setOrderNumber(
                req.getOrderNumber()
        );

        dto.setServiceName(
                req.getServiceOption() != null
                        ? req.getServiceOption().name()
                        : null
        );

        dto.setCustomerStatus(
                req.getCustomerStatus() != null
                        ? req.getCustomerStatus().name()
                        : null
        );



        dto.setRequestState(
                mapRequestState(req)
        );

        dto.setTotalPrice(
                req.getFinalPrice() != null
                        ? req.getFinalPrice().doubleValue()
                        : req.getEstimatedPrice()
        );



        if (req.getLocation() != null) {
            dto.setLocation(mapLocation(req.getLocation()));

        }
        if (req.getCar() != null) {

            dto.setPlateNumberArabic(
                    formatArabicPlate(req.getCar().getPlateNumberArabic())
            );

            dto.setPlateNumberEnglish(
                    formatEnglishPlate(req.getCar().getPlateNumberEnglish())
            );

            dto.setRequestState(
                    mapRequestState(req)
            );
        }

        return dto;
    }


    private String mapRequestState(
            CarServiceRequest req
    ) {

        if (req.getCustomerStatus()
                == CustomerRequestStatus.CANCELED) {

            return RequestState.CANCELLED.name();
        }

        if (req.getCustomerStatus()
                == CustomerRequestStatus.DELIVERED) {

            return RequestState.COMPLETED.name();
        }

        return RequestState.ACTIVE.name();
    }

    @Transactional
    public void cancelRequest(
            Integer customerId,
            Integer requestId,
            CancelRequestDto dto
    ) {

        CarServiceRequest req =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود")
                        );

        if (!req.getCustomer().getId()
                .equals(customerId)) {

            throw new ApiException("غير مصرح");
        }

        if (
                req.getCustomerStatus() == CustomerRequestStatus.CAR_RECEIVED
                        || req.getCustomerStatus() == CustomerRequestStatus.CAR_INSPECTION
                        || req.getCustomerStatus() == CustomerRequestStatus.WAITING_APPROVAL
                        || req.getCustomerStatus() == CustomerRequestStatus.UNDER_REPAIR
                        || req.getCustomerStatus() == CustomerRequestStatus.READY_FOR_DELIVERY
                        || req.getCustomerStatus() == CustomerRequestStatus.DELIVERED
        ) {

            throw new ApiException(
                    "لا يمكن إلغاء الطلب بعد استلام السيارة"
            );
        }

        if (dto.getReason() == CancelReason.OTHER &&
                (dto.getOtherReason() == null
                        || dto.getOtherReason().isBlank())) {

            throw new ApiException(
                    "يرجى كتابة سبب الإلغاء"
            );
        }

        req.setCustomerStatus(
                CustomerRequestStatus.CANCELED
        );

        req.setStage(
                WorkflowStage.CANCELLED
        );

        requestRepository.save(req);
        socketService.send(
                "/topic/past-orders/" + req.getCustomer().getUser().getId(),
                toHistoryDto(req)
        );
    }




    @Transactional
    public void addReview(
            Integer customerId,
            Integer requestId,
            RequestReviewDto dto
    ) {

        CarServiceRequest request =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود")
                        );

        if (!request.getCustomer().getId()
                .equals(customerId)) {

            throw new ApiException("غير مصرح");
        }

        if (request.getCustomerStatus()
                != CustomerRequestStatus.DELIVERED) {

            throw new ApiException(
                    "لا يمكن تقييم طلب غير منتهي"
            );
        }

        if (reviewRepository.existsByRequestId(requestId)) {

            throw new ApiException(
                    "تم تقييم الطلب مسبقاً"
            );
        }

        if (dto.getRating() < 1
                || dto.getRating() > 5) {

            throw new ApiException(
                    "التقييم يجب أن يكون من 1 إلى 5"
            );
        }

        if (dto.getRating() <= 3 &&
                (dto.getComment() == null
                        || dto.getComment().isBlank())) {

            throw new ApiException(
                    "يرجى كتابة سبب التقييم"
            );
        }

        RequestReview review =
                new RequestReview();

        review.setRequest(request);
        review.setCustomer(request.getCustomer());

        review.setRating(dto.getRating());

        review.setComment(dto.getComment());

        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
    }

    private CustomerRequestStatus mapToCustomerStatus(WorkflowStage stage) {

        if (stage == null) return CustomerRequestStatus.REQUEST_CREATED;

        return switch (stage) {

            // 🟡 بداية الطلب
            case NEW_REQUEST, ASSIGNED ->
                    CustomerRequestStatus.REQUEST_CREATED;

            // 🚗 استلام السيارة
            case RECEIVED ->
                    CustomerRequestStatus.CAR_RECEIVED;

            // 🔧 كل مراحل الفحص والتجهيز الداخلية
            case INSPECTION_IN_PROGRESS,
                 TESTING,
                 REPORT_WRITING,
                 PARTS_REGISTERING,
                 PRICING ->
                    CustomerRequestStatus.CAR_INSPECTION;

            // ⏳ انتظار موافقة العميل
            case WAITING_APPROVAL ->
                    CustomerRequestStatus.WAITING_APPROVAL;

            // 🛠️ الإصلاح
            case REPAIRING ->
                    CustomerRequestStatus.UNDER_REPAIR;

            // ✅ جاهز للتسليم
            case READY ->
                    CustomerRequestStatus.READY_FOR_DELIVERY;

            // 🚚 تم التسليم
            case DELIVERED ->
                    CustomerRequestStatus.DELIVERED;

            // ❌ إلغاء
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

        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ApiException("السيارة غير موجودة"));

        req.setCar(car);

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
        req.setOrderNumber(
                String.format("ORD-%06d", req.getId())
        );
        req.setCustomerStatus(CustomerRequestStatus.REQUEST_CREATED);
        req.setCreatedAt(LocalDateTime.now());

        return req;
    }

}