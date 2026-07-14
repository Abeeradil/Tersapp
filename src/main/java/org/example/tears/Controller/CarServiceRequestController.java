package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.*;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.InpDTO.PreviewRequestDto;
import org.example.tears.InpDTO.UpdateRequestDto;
import org.example.tears.Model.Appointment;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.OutDTO.PreviewResponseDto;
import org.example.tears.Service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/tears/service-request")
@RequiredArgsConstructor
public class CarServiceRequestController {

        private final CarServiceRequestService requestService;
        private final AppointmentService appointmentService;
        private final LocationService locationService;
        private final RequestApprovalService requestApprovalService;

        private final AuthService authService;

        @PostMapping("/preview")
        public ResponseEntity<?> preview(@RequestBody PreviewRequestDto dto) {
            return ResponseEntity.ok(requestService.preview(dto));
        }

        @PostMapping("/create")
        public ResponseEntity<?> create(
                HttpServletRequest request,
                @RequestBody CreateRequestStepDto dto) {

            return ResponseEntity.ok(requestService.createRequest(request, dto));
        }



    @GetMapping("/availability")
    public ResponseEntity<?> getAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
                appointmentService.getAvailability(date)
        );
    }

        @GetMapping("/services")
        public ApiResponse getServices() {
            return new ApiResponse(true, ServiceOption.values());
        }

        @GetMapping("/my-locations")
        public ResponseEntity<?> getMyLocations(HttpServletRequest request) {
            return ResponseEntity.ok(locationService.getMyLocations(request));
        }

    @PatchMapping("/update/{requestId}")
    public ResponseEntity<?> updateRequest(
            HttpServletRequest request,
            @PathVariable Integer requestId,
            @RequestBody UpdateRequestDto dto
    ) {

        return ResponseEntity.ok(
                requestService.updateRequest(request, requestId, dto)
        );
    }

    @GetMapping("/{id}/received-image")
    public ApiResponse getReceivedImage(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        return new ApiResponse(
                true,
                "تم جلب الصورة",
                requestService.getRequestImages(id, user.getCustomer().getId())
        );
    }

        // Get my requests
        @GetMapping("/my")
        public ResponseEntity<List<RequestResponseDto>> myRequests(HttpServletRequest request) {
            var user = authService.getAuthenticatedUser(request);
            return ResponseEntity.ok(
                    requestService.getMyRequests(user.getCustomer().getId())
            );
        }
    @GetMapping("/my/current")
    public List<CurrentRequestDto> getCurrentRequests(
            HttpServletRequest request) {

        User user = authService.getAuthenticatedUser(request);

        return requestService
                .getCurrentRequests(user.getCustomer().getId());
    }

    @GetMapping("/my/past")
    public List<RequestHistoryDto> getPastRequests(
            HttpServletRequest request) {

        User user = authService.getAuthenticatedUser(request);

        return requestService
                .getPastRequests(user.getCustomer().getId());
    }

    @GetMapping("/my-request/details/{requestId}")
    public ResponseEntity<?> getRequestDetails(
            HttpServletRequest request,
            @PathVariable Integer requestId
    ) {

        User user =
                authService.getAuthenticatedUser(request);

        return ResponseEntity.ok(
                requestService.getRequestDetails(
                        user.getCustomer().getId(),
                        requestId
                )
        );
    }

    @PostMapping("/cancel/{requestId}")
    public ResponseEntity<?> cancelRequest(
            HttpServletRequest request,
            @PathVariable Integer requestId,
            @RequestBody CancelRequestDto dto
    ) {

        User user =
                authService.getAuthenticatedUser(request);

        requestService.cancelRequest(
                user.getCustomer().getId(),
                requestId,
                dto
        );

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "تم إلغاء الطلب"
                )
        );
    }

    @PostMapping("/review/{requestId}")
    public ResponseEntity<?> addReview(
            HttpServletRequest request,
            @PathVariable Integer requestId,
            @RequestBody RequestReviewDto dto
    ) {

        User user =
                authService.getAuthenticatedUser(request);

        requestService.addReview(
                user.getCustomer().getId(),
                requestId,
                dto
        );

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "شكراً لتقييمك"
                )
        );
    }








}

//@GetMapping("/appointments/my")
//        public ResponseEntity<?> getMyAppointments(HttpServletRequest request) {
//
//            User user = authService.getAuthenticatedUser(request);
//
//            return ResponseEntity.ok(
//                    appointmentService.getMyAppointments(user.getCustomer().getId())
//            );
//        }

    // apply coupon
//    @PostMapping("/{id}/apply-coupon")
//    public ResponseEntity<CarServiceRequest> applyCoupon(@PathVariable Integer id, @RequestBody Map<String,String> body) {
//        return ResponseEntity.ok(requestService.applyCoupon(id, body.get("code")));
//    }
//
//    // confirm payment
//    @PostMapping("/{id}/confirm-payment")
//    public ResponseEntity<CarServiceRequest> confirmPayment(@PathVariable Integer id, @RequestBody Map<String,Object> body) {
//        boolean paid = Boolean.parseBoolean(body.get("paid").toString());
//        return ResponseEntity.ok(requestService.confirmPayment(id, paid));
//    }
//
//    // employee changes status (protected later by security)
//    @PutMapping("/employee/{id}/status")
//    public ResponseEntity<CarServiceRequest> changeStatusByEmployee(@PathVariable Integer id, @RequestBody Map<String,String> body) {
//        ServiceStatus s = ServiceStatus.valueOf(body.get("status"));
//        return ResponseEntity.ok(requestService.updateStatus(id, s));
//    }

//}
