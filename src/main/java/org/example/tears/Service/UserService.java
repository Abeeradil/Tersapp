package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Enums.UserRole;
import org.example.tears.Enums.UserStatus;
import org.example.tears.InpDTO.UpdateProfileDTO;
import org.example.tears.Model.Customer;
import org.example.tears.Model.Employee;
import org.example.tears.Model.User;
import org.example.tears.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

        // ================= Get Profile =================
        public ApiResponse getEmProfile(HttpServletRequest request) {

            User user = authService.getAuthenticatedUser(request);

            // 🔐 السماح فقط للموظف أو الأدمن
            if (user.getRole() != UserRole.EMPLOYEE && user.getRole() != UserRole.ADMIN) {
                throw new ApiException("Unauthorized access");
            }

            Map<String, Object> data = new HashMap<>();

            // 👤 بيانات مشتركة للجميع
            data.put("fullName", user.getFullName());
            data.put("email", user.getEmail());
            data.put("role", user.getRole());

            // 👨‍💼 بيانات الموظف فقط
            if (user.getRole() == UserRole.EMPLOYEE && user.getEmployee() != null) {
                data.put("mustChangePassword", user.getEmployee().getMustChangePassword());
            } else {
                data.put("mustChangePassword", null);
            }

            // 🧑‍💼 بيانات الأدمن (إذا تبغين تضيفين لاحقًا)
            if (user.getRole() == UserRole.ADMIN) {
                data.put("adminAccess", true);
            }

            return new ApiResponse(true, data);
        }

    public ApiResponse getCusProfile(HttpServletRequest request) {

        User user = authService.getAuthenticatedUser(request);

        if (user.getRole() != UserRole.CUSTOMER)
            throw new ApiException("Unauthorized access");

        Customer customer = user.getCustomer();

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", user.getFullName());
        data.put("phoneNumber", user.getPhoneNumber());
        data.put("dateOfBirth", customer != null ? customer.getDateOfBirth() : null);

        return new ApiResponse(true, data);
    }


    // ================= Update Profile =================
    public ApiResponse updateProfile(HttpServletRequest request, UpdateProfileDTO dto) {

        User user = authService.getAuthenticatedUser(request);
        Customer customer = user.getCustomer();

        // 🟢 الاسم (partial update)
        if (dto.getFirstName() != null ||
                dto.getMiddleName() != null ||
                dto.getLastName() != null) {

            String[] parts = user.getFullName() != null
                    ? user.getFullName().split(" ")
                    : new String[]{"", "", ""};

            String first = dto.getFirstName() != null ? dto.getFirstName() : parts[0];
            String middle = dto.getMiddleName() != null ? dto.getMiddleName() : (parts.length > 1 ? parts[1] : "");
            String last = dto.getLastName() != null ? dto.getLastName() : (parts.length > 2 ? parts[2] : "");

            user.setFullName((first + " " + middle + " " + last).trim());
        }

        // 🟢 تاريخ الميلاد
        if (dto.getDateOfBirth() != null) {
            customer.setDateOfBirth(dto.getDateOfBirth());
        }

        userRepository.save(user);

        return new ApiResponse(true, "Profile updated successfully");
    }
    // ================= Change Phone (Step 1) =================
    public ApiResponse requestChangePhone(HttpServletRequest request, String newPhone) {

        User user = authService.getAuthenticatedUser(request);

        // تحقق أن الرقم غير مستخدم
        if (userRepository.existsByPhoneNumber(newPhone)) {
            throw new ApiException("رقم الجوال مستخدم");
        }

        // خزّن الرقم مؤقت
        user.setPendingPhoneNumber(newPhone);
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        userRepository.save(user);

        // ================= DEV =================
        System.out.println("OTP = 123456");

        // ================= PRODUCTION =================
        // Verification.creator(
        //     twilioConfig.getServiceSid(),
        //     newPhone,
        //     "sms"
        // ).create();

        return new ApiResponse(true, "OTP sent to new phone");
    }
    // ================= Change Phone (Step 2) =================
    public ApiResponse confirmChangePhone(HttpServletRequest request, String otp) {

        User user = authService.getAuthenticatedUser(request);

        if (user.getPendingPhoneNumber() == null) {
            throw new ApiException("No phone change request found");
        }

        // ================= DEV =================
        if (!otp.equals("123456")) {
            throw new ApiException("Invalid OTP");
        }

        // ================= PRODUCTION =================
        // VerificationCheck check = VerificationCheck.creator(twilioConfig.getServiceSid())
        //        .setTo(user.getPendingPhoneNumber())
        //        .setCode(otp)
        //        .create();
        //
        // if (!"approved".equalsIgnoreCase(check.getStatus()))
        //     throw new ApiException("Invalid or expired OTP");

        // تحديث الرقم
        user.setPhoneNumber(user.getPendingPhoneNumber());
        user.setPendingPhoneNumber(null);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        return new ApiResponse(true, "Phone updated successfully");
    }



        // ================= Update Notifications =================
        public ApiResponse updateNotifications(HttpServletRequest request, Boolean enabled) {

            User user = authService.getAuthenticatedUser(request);

            if (enabled == null) {
                throw new ApiException("قيمة الإشعارات مطلوبة");
            }

            user.setNotificationsEnabled(enabled);
            userRepository.save(user);

            return new ApiResponse(true, "Notifications updated successfully", enabled);
        }

    public ApiResponse makeOneAdmin(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        return new ApiResponse(true, "User promoted to ADMIN");
    }
}
