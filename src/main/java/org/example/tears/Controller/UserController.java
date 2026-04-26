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
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getProfile(request));
    }

    // update profile
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateProfile(
            HttpServletRequest request,
            @RequestBody UpdateProfileDTO dto) {

        ApiResponse response = userService.updateProfile(request, dto);
        return ResponseEntity.ok(response);
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
