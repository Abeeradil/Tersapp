package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Enums.UserRole;
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

        // 🔥 دمج الاسم
        String fullName = String.join(" ",
                Optional.ofNullable(dto.getFirstName()).orElse(""),
                Optional.ofNullable(dto.getMiddleName()).orElse(""),
                Optional.ofNullable(dto.getLastName()).orElse("")
        ).trim();

        if (!fullName.isBlank())
            user.setFullName(fullName);

        if (dto.getPhoneNumber() != null)
            user.setPhoneNumber(dto.getPhoneNumber());

        userRepository.save(user);

        return new ApiResponse(true, "Profile updated successfully");
    }

        // ================= Update Notifications =================
        public ApiResponse updateNotifications(HttpServletRequest request, Boolean enabled) {

            User user = authService.getAuthenticatedUser(request);

            user.setNotificationsEnabled(enabled);

            userRepository.save(user);

            return new ApiResponse(true,"Notifications updated successfully");
        }
    public ApiResponse makeOneAdmin(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        return new ApiResponse(true, "User promoted to ADMIN");
    }
}
