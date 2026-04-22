package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.PhoneNumberDTO;
import org.example.tears.DTO.VerifyChangePasswordDTO;
import org.example.tears.DTO.VerifyOtpDTO;
import org.example.tears.InpDTO.ChangePasswordDTO;
import org.example.tears.InpDTO.CustomerRegisterDTO;
import org.example.tears.InpDTO.EmployeeRegisterDTO;
import org.example.tears.InpDTO.LoginDTO;
import org.example.tears.Model.Customer;
import org.example.tears.Model.Employee;
import org.example.tears.Model.User;
import org.example.tears.Model.VerifyOtpRequest;
import org.example.tears.OutDTO.AuthStatusDto;
import org.example.tears.OutDTO.LoginResponse;
import org.example.tears.Repository.UserRepository;
import org.example.tears.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/tears/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ================= Customer =================
    // تسجيل عميل جديد + إرسال OTP
    @PostMapping("/customer/register")
    public ApiResponse registerCustomer(@RequestBody CustomerRegisterDTO dto) {
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

    // إرسال OTP لتغيير كلمة المرور
    @PostMapping("/password/otp/send")
    public ApiResponse sendOtpForPasswordReset(
            @RequestBody PhoneNumberDTO dto
    ) {
        return authService.sendOtpForPasswordChange(dto.getPhoneNumber());
    }

    // التحقق من OTP + تغيير كلمة المرور
    @PostMapping("/password/otp/verify")
    public ApiResponse verifyOtpAndChangePassword(
            @RequestBody VerifyChangePasswordDTO dto
    ) {
        return authService.verifyOtpAndChangePassword(dto);
    }

    // ================= Get Logged User =================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getMe(request));
    }

    @DeleteMapping("/dev/delete/{phone}")
    public ApiResponse deleteByPhone(@PathVariable String phone) {
        authService.deleteByPhone(phone);
        return new ApiResponse("Deleted");
    }
}
