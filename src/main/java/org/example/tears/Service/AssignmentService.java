package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.AssignmentStatus;
import org.example.tears.Enums.EmployeeRole;
import org.example.tears.Enums.UserRole;
import org.example.tears.Enums.WorkflowStage;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestAssignment;
import org.example.tears.Model.User;
import org.example.tears.Repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final CarServiceRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final AssignmentRepository assignmentRepo;
    private final RequestAssignmentRepository reqAssignmentRepo;
    private final NotificationService notificationService;

    @Transactional
    public void assign(Integer requestId, Integer employeeId) {

        CarServiceRequest request = requestRepo.findById(requestId)
                .orElseThrow();

        User user = userRepo.findById(employeeId)
                .orElseThrow();

        if (user.getRole() != UserRole.EMPLOYEE) {
            throw new RuntimeException("ليس موظف");
        }

        Employee employee = user.getEmployee(); // 🔥 مهم

        // ✅ حدث الطلب مباشرة
        request.setAssignedEmployee(employee);
        request.setStage(WorkflowStage.NEW_REQUEST);

        requestRepo.save(request);

        notificationService.send(user, "تم إسناد طلب جديد لك");
    }

}
