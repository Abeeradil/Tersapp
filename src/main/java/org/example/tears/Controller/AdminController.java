package org.example.tears.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.RequestSummaryDto;
import org.example.tears.InpDTO.AdminCreateEmployeeDTO;
import org.example.tears.Model.Appointment;
import org.example.tears.Model.Employee;
import org.example.tears.OutDTO.EmployeeLoginInfo;
import org.example.tears.Service.AdminService;
import org.example.tears.Service.AppointmentService;
import org.example.tears.Service.AssignmentService;
import org.example.tears.Service.RequestQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
    @RequestMapping("api/v1/tears/dashboard")
    @RequiredArgsConstructor
    @PreAuthorize("hasRole('ADMIN')")

public class AdminController {

    private final AdminService adminService;
    private final AssignmentService assignmentService;
    private final RequestQueryService requestQueryService;

    @GetMapping("/all/requests")
    public List<RequestSummaryDto> getAll() {
        return requestQueryService.getAllRequests();
    }

    @GetMapping("/requests/unassigned")
    public List<RequestSummaryDto> unassigned() {
        return requestQueryService.getUnassigned();
    }

    @PostMapping("/assign")
    public ApiResponse assign(@RequestParam Integer requestId,
                              @RequestParam Integer employeeId) {
        assignmentService.assign(requestId, employeeId);
        return new ApiResponse(true, "تم الإسناد");
    }

    @GetMapping("/employees")
    public List<Employee> employees() {
        return adminService.getAllEmployees();
    }


    // جلب كل الطلبات
//    @GetMapping("/admin/requests")
//    public ResponseEntity<?> getAllRequests() {
//        var requests = adminService.getAllRequests();
//
//        if (requests.isEmpty()) {
//            return ResponseEntity.ok(Map.of(
//                    "success", false,
//                    "message", "لا توجد طلبات حالياً"
//            ));
//        }
//
//        return ResponseEntity.ok(Map.of(
//                "success", true,
//                "data", requests
//        ));
//    }

    @PostMapping("/admin/assign")
    public ApiResponse assignRequest(
            @RequestParam Integer requestId,
            @RequestParam Integer employeeId
    ) {

        assignmentService.assign(requestId, employeeId);

        return new ApiResponse(
                true,
                "تم إسناد الطلب بنجاح"
        );
    }

    @PostMapping("/admin/add/employee")
    public ResponseEntity<EmployeeLoginInfo> createEmployee(
            @Valid @RequestBody AdminCreateEmployeeDTO dto
    ) {

        return ResponseEntity.ok(
                adminService.createEmployee(dto)
        );
    }


    @GetMapping("/admin/employees")
    public List<Employee> getEmployees() {
        return adminService.getAllEmployees();
    }

    @PutMapping("/admin/employees/{id}/deactivate")
        public ApiResponse deactivate(@PathVariable Integer id) {
            return adminService.deactivateEmployee(id);
        }

}
