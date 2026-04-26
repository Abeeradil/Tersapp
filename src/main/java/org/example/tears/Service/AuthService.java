package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Config.TwilioConfig;
import org.example.tears.DTO.VerifyChangePasswordDTO;
import org.example.tears.Enums.UserRole;
import org.example.tears.Enums.UserStatus;
import org.example.tears.InpDTO.ChangePasswordDTO;
import org.example.tears.InpDTO.CustomerRegisterDTO;
import org.example.tears.InpDTO.LoginDTO;
import org.example.tears.Model.Customer;
import org.example.tears.Model.JwtUtil;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.AuthStatusDto;
import org.example.tears.Repository.CustomerRepository;
import org.example.tears.Repository.EmployeeRepository;
import org.example.tears.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final EmployeeRepository employeeRepo;
    private final PasswordEncoder encoder;
    private final TwilioConfig twilioConfig;
    private final JwtUtil jwtUtil;

    // =========================================================
    // 1️⃣ تسجيل العميل
    // =========================================================
    public ApiResponse registerCustomer(CustomerRegisterDTO dto) {

        if (userRepo.existsByPhoneNumber(dto.getPhoneNumber()))
            throw new ApiException("Phone already used");

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setPassword(encoder.encode("TEMP@1234"));

        Customer customer = new Customer();
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setUser(user);

        user.setCustomer(customer);

        userRepo.save(user);

        // ================= DEV =================
        System.out.println("OTP = 123456");

        // ================= PRODUCTION =================
        // try {
        //     Verification.creator(
        //         twilioConfig.getServiceSid(),
        //         dto.getPhoneNumber(),
        //         "sms"
        //     ).create();
        // } catch (Exception e) {
        //     throw new ApiException("Failed to send OTP");
        // }

        return new ApiResponse(true, "OTP sent to " + dto.getPhoneNumber());
    }

    // =========================================================
    // 2️⃣ التحقق من OTP
    // =========================================================
    public ApiResponse verifyCustomerOtp(String phoneNumber, String otp) {

        User user = userRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException("User not found"));

        // ================= DEV =================
        if (!otp.equals("123456"))
            throw new ApiException("Invalid OTP");

        // ================= PRODUCTION =================
        // VerificationCheck check = VerificationCheck.creator(twilioConfig.getServiceSid())
        //        .setTo(phoneNumber)
        //        .setCode(otp)
        //        .create();
        //
        // if (!"approved".equalsIgnoreCase(check.getStatus()))
        //     throw new ApiException("Invalid or expired OTP");

        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);

        String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole().name());

        return new ApiResponse(true, "User verified successfully", token);
    }

    // =========================================================
    // 3️⃣ إعادة إرسال OTP
    // =========================================================
    public ApiResponse resendCustomerOtp(String phoneNumber) {

        User user = userRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException("User not found"));

        if (user.getRole() != UserRole.CUSTOMER)
            throw new ApiException("Not a customer");

//        if (user.getStatus() == UserStatus.ACTIVE)
//            throw new ApiException("Account already verified");

        // ================= DEV =================
        System.out.println("OTP resent = 123456");

        // ================= PRODUCTION =================
        // try {
        //     Verification.creator(
        //         twilioConfig.getServiceSid(),
        //         phoneNumber,
        //         "sms"
        //     ).create();
        // } catch (Exception e) {
        //     throw new ApiException("Failed to resend OTP");
        // }

        return new ApiResponse(true, "OTP resent to " + phoneNumber);
    }

    // =========================================================
    // 4️⃣ تسجيل دخول العميل
    // =========================================================
    public ApiResponse loginCustomer(String phoneNumber) {

        User user = userRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new ApiException("Phone number not registered. Please register first.")
                );

        if (user.getStatus() != UserStatus.ACTIVE)
            throw new ApiException("Account not active. Please verify OTP first.");

        // ================= DEV =================
        System.out.println("OTP login = 123456");

        // ================= PRODUCTION =================
        // Verification.creator(
        //     twilioConfig.getServiceSid(),
        //     phoneNumber,
        //     "sms"
        // ).create();

        return new ApiResponse(true, "OTP sent to " + phoneNumber);
    }

    // =========================================================
    // 5️⃣ تسجيل دخول الموظف
    // =========================================================
    public ApiResponse loginEmployee(LoginDTO dto) {

        User user = userRepo
                .findByEmailOrPhoneNumber(dto.getEmailOrPhone(), dto.getEmailOrPhone())
                .orElseThrow(() -> new ApiException("بيانات الدخول غير صحيحة"));

        if (!encoder.matches(dto.getPassword(), user.getPassword()))
            throw new ApiException("بيانات الدخول غير صحيحة");

        if (user.getEmployee() != null && user.getEmployee().getMustChangePassword()) {
            return new ApiResponse(true, "يجب تغيير كلمة المرور أول مرة");
        }

        if (user.getStatus() != UserStatus.ACTIVE)
            throw new ApiException("الحساب غير مفعل");

        String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole().name());

        return new ApiResponse(true, "تم تسجيل الدخول بنجاح", token);
    }

    // =========================================================
    // 6️⃣ تغيير كلمة المرور
    // =========================================================
    public ApiResponse changePassword(HttpServletRequest request, ChangePasswordDTO dto) {

        User user = getAuthenticatedUser(request);

        if (!encoder.matches(dto.getOldPassword(), user.getPassword()))
            throw new ApiException("كلمة المرور القديمة غير صحيحة");

        user.setPassword(encoder.encode(dto.getNewPassword()));

        if (user.getEmployee() != null) {
            user.getEmployee().setMustChangePassword(false);
            user.setStatus(UserStatus.ACTIVE);
        }

        userRepo.save(user);

        String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole().name());

        return new ApiResponse(true, "تم تغيير كلمة المرور بنجاح", token);
    }

    // =========================================================
    // 7️⃣ OTP لتغيير كلمة المرور
    // =========================================================
    public ApiResponse sendOtpForPasswordChange(String phoneNumber) {

        userRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException("User not found"));

        System.out.println("OTP password change = 123456");

        return new ApiResponse(true, "OTP sent to " + phoneNumber);
    }

    public ApiResponse verifyOtpAndChangePassword(VerifyChangePasswordDTO dto) {

        User user = userRepo.findByPhoneNumber(dto.getPhoneNumber())
                .orElseThrow(() -> new ApiException("User not found"));

        if (!dto.getOtp().equals("123456"))
            throw new ApiException("Invalid OTP");

        if (!dto.getNewPassword().equals(dto.getConfirmPassword()))
            throw new ApiException("Passwords do not match");

        user.setPassword(encoder.encode(dto.getNewPassword()));

        if (user.getEmployee() != null)
            user.getEmployee().setMustChangePassword(false);

        if (user.getStatus() != UserStatus.ACTIVE)
            user.setStatus(UserStatus.ACTIVE);

        userRepo.save(user);

        String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole().name());

        return new ApiResponse(true, "تم تغيير كلمة المرور بنجاح", token);
    }

    // =========================================================
    // 8️⃣ جلب المستخدم من التوكن
    // =========================================================
    public User getAuthenticatedUser(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer "))
            throw new ApiException("Missing token", 401);

        String token = header.substring(7);
        String phone = jwtUtil.getPhoneFromToken(token);

        return userRepo.findByPhoneNumber(phone)
                .orElseThrow(() -> new ApiException("User not found", 404));
    }

    // =========================================================
    // 9️⃣ حذف مستخدم (اختياري)
    // =========================================================
    @Transactional
    public ApiResponse deleteByPhone(String phone) {
        userRepo.deleteByPhoneNumber(phone);
        return new ApiResponse(true, "Deleted");
    }

    // =========================================================
    // 🔟 حالة المستخدم
    // =========================================================
    public ApiResponse getMe(HttpServletRequest request) {

        try {
            User user = getAuthenticatedUser(request);

            return new ApiResponse(
                    true,
                    new AuthStatusDto(
                            true,
                            user.getId(),
                            user.getFullName(),
                            user.getRole().name()
                    )
            );

        } catch (Exception e) {

            return new ApiResponse(
                    false,
                    new AuthStatusDto(
                            false,
                            null,
                            null,
                            "GUEST"
                    )
            );
        }
    }
}