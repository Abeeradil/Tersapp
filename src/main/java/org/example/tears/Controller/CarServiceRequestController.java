package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.InpDTO.PreviewRequestDto;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.OutDTO.PreviewResponseDto;
import org.example.tears.Service.AuthService;
import org.example.tears.Service.CarServiceRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tears/service-request")
@RequiredArgsConstructor
public class CarServiceRequestController {

    private final CarServiceRequestService requestService;
    private final AuthService authService;

        // Step 1: Preview
        @PostMapping("/preview")
        public ResponseEntity<PreviewResponseDto> preview(
                @Valid @RequestBody PreviewRequestDto dto) {
            return ResponseEntity.ok(requestService.preview(dto));
        }


    // Step 2: Create final request
        @PostMapping("/create")
        public ResponseEntity<RequestResponseDto> create(
                HttpServletRequest request,
                @Valid @RequestBody CreateRequestStepDto dto) {

            return ResponseEntity.ok(requestService.createRequest(request, dto));
        }

        // Get my requests
//        @GetMapping("/my")
//        public ResponseEntity<List<RequestResponseDto>> myRequests(HttpServletRequest request) {
//            var user = authService.getAuthenticatedUser(request);
//            return ResponseEntity.ok(
//                    requestService.getMyRequests(user.getCustomer().getId())
//            );
//        }
    }


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
