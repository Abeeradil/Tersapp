package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Enums.UserRole;
import org.example.tears.InpDTO.UpdateProfileDTO;
import org.example.tears.Model.User;
import org.example.tears.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

        // ================= Get Profile =================
        public Map<String, Object> getProfile(HttpServletRequest request) {
            User user = authService.getAuthenticatedUser(request);

            String fullName = user.getFullName();

            String dateOfBirth = null;
            if (user.getRole() == UserRole.CUSTOMER && user.getCustomer() != null) {
                dateOfBirth = user.getCustomer().getDateOfBirth().toString(); // أو حسب نوع البيانات
            }

            return Map.of(
                    "fullName", fullName,
                    "phoneNumber", user.getPhoneNumber(),
                    "dateOfBirth", dateOfBirth,
                    "notificationsEnabled", user.getNotificationsEnabled()
            );
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
