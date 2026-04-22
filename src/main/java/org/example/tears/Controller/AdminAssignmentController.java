package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Service.AssignmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tears/admin/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAssignmentController {

        private final AssignmentService assignmentService;
            // -----------------------
            // إسناد يدوي
            // -----------------------
            @PostMapping("/{requestId}/{employeeId}")
            public ApiResponse assign(
                    @PathVariable Integer requestId,
                    @PathVariable Integer employeeId) {

                assignmentService.assign(requestId, employeeId);
                return new ApiResponse("تم إسناد الطلب بنجاح");
            }


            @PostMapping("/test/{requestId}/{employeeId}")
            public ApiResponse assignTest(
                    @PathVariable Integer requestId,
                    @PathVariable Integer employeeId) {

                assignmentService.assign(requestId, employeeId);
                return new ApiResponse("تم إسناد الطلب بنجاح (Test Mode)");
            }}