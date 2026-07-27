package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Enums.UserRole;
import org.example.tears.Enums.UserStatus;
import org.example.tears.InpDTO.UpdateEmployeeProfileDTO;
import org.example.tears.InpDTO.UpdateProfileDTO;
import org.example.tears.Model.Customer;
import org.example.tears.Model.User;
import org.example.tears.Repository.CarRepository;
import org.example.tears.Repository.CustomerRepository;
import org.example.tears.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CarRepository carRepository;
    private final AuthService authService;

        // ================= Get Profile =================
        public ApiResponse getEmProfile(
                HttpServletRequest request
        ) {

            User user =
                    authService.getAuthenticatedUser(
                            request
                    );

            if (
                    user.getRole() != UserRole.EMPLOYEE
                            &&
                            user.getRole() != UserRole.ADMIN
            ) {

                throw new ApiException(
                        "Unauthorized access"
                );
            }

            String fullName =
                    user.getFullName() != null
                            ? user.getFullName().trim()
                            : "";

            String[] parts =
                    fullName.split("\\s+");

            String firstName =
                    parts.length > 0
                            ? parts[0]
                            : "";

            String middleName =
                    parts.length > 1
                            ? parts[1]
                            : "";

            String lastName =
                    parts.length > 2
                            ? String.join(
                            " ",
                            Arrays.copyOfRange(
                                    parts,
                                    2,
                                    parts.length
                            )
                    )
                            : "";

            Map<String,Object> data =
                    new HashMap<>();

            data.put(
                    "firstName",
                    firstName
            );

            data.put(
                    "middleName",
                    middleName
            );

            data.put(
                    "lastName",
                    lastName
            );

            data.put(
                    "email",
                    user.getEmail()
            );

            data.put(
                    "userID",
                    user.getId()
            );

            data.put(
                    "empID",
                    user.getEmployee().getId()
            );

            data.put(
                    "jobTitle",
                    user.getEmployee() != null
                            ? user.getEmployee()
                            .getJobTitle()
                            : null
            );

            return new ApiResponse(
                    true,
                    data
            );
        }
    public ApiResponse updateEmployeeProfile(
            HttpServletRequest request,
            UpdateEmployeeProfileDTO dto
    ) {

        User user =
                authService.getAuthenticatedUser(
                        request
                );

        if (
                user.getRole() != UserRole.EMPLOYEE
                        &&
                        user.getRole() != UserRole.ADMIN
        ) {

            throw new ApiException(
                    "Unauthorized"
            );
        }

        String fullName =
                user.getFullName() != null
                        ? user.getFullName().trim()
                        : "";

        String[] parts =
                fullName.split("\\s+");

        String currentFirst =
                parts.length > 0 ? parts[0] : "";

        String currentMiddle =
                parts.length > 1 ? parts[1] : "";

        String currentLast =
                parts.length > 2
                        ? String.join(
                        " ",
                        Arrays.copyOfRange(
                                parts,
                                2,
                                parts.length
                        )
                )
                        : "";

        String first =
                dto.getFirstName() != null
                        ? dto.getFirstName()
                        : currentFirst;

        String middle =
                dto.getMiddleName() != null
                        ? dto.getMiddleName()
                        : currentMiddle;

        String last =
                dto.getLastName() != null
                        ? dto.getLastName()
                        : currentLast;

        user.setFullName(
                (first + " " + middle + " " + last)
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        )
        );

        userRepository.save(user);

        return new ApiResponse(
                true,
                "Profile updated"
        );
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

        // ================= CURRENT FULL NAME =================
        String fullName = user.getFullName() != null
                ? user.getFullName().trim()
                : "";

        String[] parts = fullName.split("\\s+");

        String currentFirst = parts.length > 0 ? parts[0] : "";
        String currentMiddle = parts.length > 1 ? parts[1] : "";
        String currentLast = parts.length > 2
                ? String.join(" ", Arrays.copyOfRange(parts, 2, parts.length))
                : "";

        // ================= UPDATE NAME =================
        String firstName = dto.getFirstName() != null
                ? dto.getFirstName().trim()
                : currentFirst;

        String middleName = dto.getMiddleName() != null
                ? dto.getMiddleName().trim()
                : currentMiddle;

        String lastName = dto.getLastName() != null
                ? dto.getLastName().trim()
                : currentLast;

        String newFullName = (firstName + " " + middleName + " " + lastName)
                .trim()
                .replaceAll("\\s+", " ");

        user.setFullName(newFullName);

        // ================= DATE OF BIRTH =================
        if (dto.getDateOfBirth() != null) {
            customer.setDateOfBirth(dto.getDateOfBirth());
        }

        // ================= SAVE =================
        userRepository.save(user);
        customerRepository.save(customer);

        return new ApiResponse(true, "✅ Profile updated successfully");
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
    @Transactional
    public void deleteUser(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("❌ User not found"));

        Customer customer = user.getCustomer();

        if (customer != null) {
            carRepository.deleteAllByCustomerId(customer.getId());
            customerRepository.delete(customer);
        }

        userRepository.delete(user);
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
