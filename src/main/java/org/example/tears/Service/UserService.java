package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Enums.UserRole;
import org.example.tears.Model.User;
import org.example.tears.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

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
        public ApiResponse updateProfile(HttpServletRequest request, User updatedUser) {

            User user = authService.getAuthenticatedUser(request);

            if (updatedUser.getFullName() != null)
                user.setFullName(updatedUser.getFullName());

            if (updatedUser.getPhoneNumber() != null)
                user.setPhoneNumber(updatedUser.getPhoneNumber());

            userRepository.save(user);

            return new ApiResponse("Profile updated successfully");
        }

        // ================= Update Notifications =================
        public ApiResponse updateNotifications(HttpServletRequest request, Boolean enabled) {

            User user = authService.getAuthenticatedUser(request);

            user.setNotificationsEnabled(enabled);

            userRepository.save(user);

            return new ApiResponse("Notifications updated successfully");
        }
    }
