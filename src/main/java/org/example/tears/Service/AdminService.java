package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Config.PasswordGenerator;
import org.example.tears.Config.TempEmailGenerator;
import org.example.tears.Enums.UserRole;
import org.example.tears.Enums.UserStatus;
import org.example.tears.InpDTO.AdminCreateEmployeeDTO;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.EmployeeLoginInfo;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.EmployeeRepository;
import org.example.tears.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final EmployeeRepository employeeRepository;
    private final CarServiceRequestRepository reqRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passGen;
    private final TempEmailGenerator emailGen;
    private final UserRepository userRepo;


    // ================= Employee (Admin registers only) =================
    public EmployeeLoginInfo createEmployee(AdminCreateEmployeeDTO dto) {

        if (userRepo.existsByPhoneNumber(dto.getPhoneNumber()))
            throw new ApiException("Phone already used");

        String email = emailGen.generate(dto.getFullName());
        String rawPass = passGen.generate();

        User user = new User();

        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPass));
        user.setRole(UserRole.EMPLOYEE);
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        Employee emp = new Employee();
        emp.setJobTitle(dto.getJobTitle());
        emp.setMustChangePassword(true);
        emp.setUser(user);

        user.setEmployee(emp);

        userRepo.save(user);

        return new EmployeeLoginInfo(email, rawPass);
    }


    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }


    // -----------------------
    // جلب كل الطلبات
    // -----------------------
    public List<RequestResponseDto> getAllRequests() {
        return reqRepo.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // -----------------------
    // تحويل CarServiceRequest -> RequestResponseDto
    // -----------------------
    private RequestResponseDto toResponseDto(CarServiceRequest r) {
        RequestResponseDto dto = new RequestResponseDto();

        dto.setId(r.getId());
        dto.setOrderNumber(r.getOrderNumber());
        dto.setStatus(r.getCustomerStatus() != null ? r.getCustomerStatus().name() : "REQUEST_CREATED");
        dto.setTotalPrice(r.getEstimatedPrice() != null ? r.getEstimatedPrice() : 0);
        dto.setAppointmentDate(r.getAppointmentDate());
        dto.setAppointmentTime(r.getAppointmentTime());
        dto.setPaymentMethod(r.getPaymentMethod() != null ? r.getPaymentMethod().name() : "UNKNOWN");
        dto.setHydraulicTruck(r.isHydraulicTruck());

        if (r.getLocation() != null) {
            dto.setLocationId(r.getLocation().getId());
            dto.setLat(r.getLocation().getLat());
            dto.setLng(r.getLocation().getLng());
            dto.setAddress(r.getLocation().getAddress());
        }

        return dto;
    }

    public ApiResponse deactivateEmployee(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException("Employee not found"));

        employee.getUser().setStatus(UserStatus.INACTIVE);
        employeeRepository.save(employee);

        return new ApiResponse("Employee deactivated");
    }
}
