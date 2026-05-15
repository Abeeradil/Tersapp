package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.InpDTO.UpdateProfileDTO;
import org.example.tears.Model.User;
import org.example.tears.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("api/v1/tears/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    // view profile
    @GetMapping("/customer/profile")
    public ResponseEntity<ApiResponse> getCustomerProfile(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getCusProfile(request));
    }

    @GetMapping("/employee/profile")
    public ResponseEntity<ApiResponse> getEmployeeProfile(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getEmProfile(request));
    }

    // update profile
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse> updateProfile(
            HttpServletRequest request,
            @RequestBody UpdateProfileDTO dto) {

        ApiResponse response = userService.updateProfile(request, dto);
        return ResponseEntity.ok(response);
    }

    // ================= Step 1 =================
    @PostMapping("/change-phone/request")
    public ResponseEntity<ApiResponse> requestChangePhone(
            HttpServletRequest request,
            @RequestParam String newPhone
    ) {
        return ResponseEntity.ok(
                userService.requestChangePhone(request, newPhone)
        );
    }

    // ================= Step 2 =================
    @PostMapping("/change-phone/confirm")
    public ResponseEntity<ApiResponse> confirmChangePhone(
            HttpServletRequest request,
            @RequestParam String otp
    ) {
        return ResponseEntity.ok(
                userService.confirmChangePhone(request, otp)
        );
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Integer userId) {

        userService.deleteUser(userId);

        return ResponseEntity.ok(
                new ApiResponse(true, "✅ User deleted successfully")
        );
    }

    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse> updateNotifications(
            HttpServletRequest request,
            @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        ApiResponse response = userService.updateNotifications(request, enabled);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/dev/make-admin/{userId}")
    public ResponseEntity<ApiResponse> makeAdmin(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.makeOneAdmin(userId));
    }
}
