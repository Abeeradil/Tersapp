package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.*;
import org.example.tears.InpDTO.ChangePasswordDTO;
import org.example.tears.InpDTO.CustomerRegisterDTO;
import org.example.tears.InpDTO.LoginDTO;
import org.example.tears.Model.JwtUtil;
import org.example.tears.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/tears/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;


    // ================= Customer =================
    // تسجيل عميل جديد + إرسال OTP
    @PostMapping("/customer/register")
    public ApiResponse registerCustomer(@Valid @RequestBody CustomerRegisterDTO dto) {
        return authService.registerCustomer(dto);
    }

    // تفعيل حساب العميل بالـ OTP
    @PostMapping("/customer/verify")
    public ApiResponse verifyCustomerOtp(@RequestBody VerifyOtpDTO dto) {
        return authService.verifyCustomerOtp(
                dto.getPhoneNumber(),
                dto.getOtp()
        );
    }

    // إعادة إرسال OTP للعميل
    @PostMapping("/customer/resend-otp")
    public ApiResponse resendCustomerOtp(@RequestBody PhoneNumberDTO dto) {
        return authService.resendCustomerOtp(dto.getPhoneNumber());
    }
    @PostMapping("/dev/admin-token")
    public ResponseEntity<?> adminToken() {

        String token = jwtUtil.generateToken(
                "+966500000009",
                "ADMIN"
        );

        return ResponseEntity.ok(
                Map.of(
                        "token",
                        token
                )
        );
    }
    // ================= General Login =================

    // تسجيل دخول عميل
    @PostMapping("/customer/login/send-otp")
    public ApiResponse loginCustomer(@RequestBody PhoneNumberDTO dto) {
        return authService.loginCustomer(dto.getPhoneNumber());
    }

    // تسجيل دخول موظف
    @PostMapping("/employee/login")
    public ApiResponse loginEmployee(@RequestBody LoginDTO dto) {
        return authService.loginEmployee(dto);
    }


    // ================= Change Password =================
    // تغيير كلمة المرور بعد تسجيل الدخول
    @PostMapping("/change-password")
    public ApiResponse changePassword(HttpServletRequest request,
                                      @RequestBody ChangePasswordDTO dto) {
        return authService.changePassword(request, dto);
    }

    // ================= OTP Password Reset (Employee) =================


    @PutMapping("/reset-password")
    public ApiResponse resetPassword(
            HttpServletRequest request,
            @RequestBody @Valid ResetPasswordDTO dto
    ){

        return authService.resetPasswordInsideApp(
                request,
                dto
        );
    }

    @PostMapping("/employee/verify")
    public ResponseEntity<?> verifyEmployeeOtp(
            @RequestBody VerifyEmployeeOtpDTO dto
    ) {

        return ResponseEntity.ok(
                authService.verifyEmployeeOtp(
                        dto.getEmailOrPhone(),
                        dto.getOtp()
                )
        );
    }
    // ================= Get Logged User =================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getMe(request));
    }

    @DeleteMapping("/dev/delete/{phone}")
    public ApiResponse deleteByPhone(@PathVariable String phone) {
        authService.deleteByPhone(phone);
        return new ApiResponse(true,"Deleted");
    }
}
