package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.EmployeeSummaryDto;
import org.example.tears.DTO.RequestSummaryDto;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Enums.WorkflowStage;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestNote;
import org.example.tears.Model.RequestStatusHistory;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestNoteRepository;
import org.example.tears.Repository.RequestStatusHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestWorkflowService {

        private final CarServiceRequestRepository requestRepo;
        private final RequestNoteRepository noteRepo;
        private final RequestStatusHistoryRepository historyRepo;
        private final NotificationService notificationService;

    public List<RequestSummaryDto> getMyRequests(Employee employee) {
        return requestRepo.findByAssignedEmployee(employee)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public RequestSummaryDto toSummaryDto(CarServiceRequest req) {

        RequestSummaryDto dto = new RequestSummaryDto();

        dto.setId(req.getId());
        dto.setOrderNumber(req.getOrderNumber());
        dto.setStatus(req.getStage().name());
        dto.setTotalPrice(
                req.getFinalPrice() != null
                        ? req.getFinalPrice().doubleValue()
                        : req.getEstimatedPrice()
        );

        if (req.getAssignedEmployee() != null) {
            dto.setAssignedEmployee(
                    req.getAssignedEmployee().getUser().getFullName()
            );
        }

        return dto;
    }

    public EmployeeSummaryDto toEmployeeSummary(Employee emp) {

        EmployeeSummaryDto dto = new EmployeeSummaryDto();

        dto.setId(emp.getId());

        // ⚠️ حسب الـ Entity عندك
        dto.setName(emp.getUser().getFullName());

        dto.setJobTitle(emp.getJobTitle());

        dto.setRole(emp.getEmployeeRole()); // إذا موجودة عندك

        return dto;
    }

        // =========================
        // تغيير حالة الموظف
        // =========================
        @Transactional
        public void updateStaffStatus(
                Integer requestId,
                StaffRequestStatus status,
                Integer employeeId,
                String note
        ) {

            CarServiceRequest req = requestRepo.findById(requestId)
                    .orElseThrow(() ->
                            new RuntimeException("الطلب غير موجود")
                    );
            // تحقق أن الطلب مسند له
            if (req.getAssignedEmployee() == null ||
                    !employeeId.equals(req.getAssignedEmployee().getId())) {

                throw new RuntimeException("غير مصرح لك");
            }


            // تحديث الحالة
            req.setStaffStatus(status);
            req.setLastUpdated(LocalDateTime.now());

            updateStaffTimestamps(req, status);

            // حفظ ملاحظة
            if (note != null && !note.isBlank()) {
                saveNote(req, employeeId, note);
            }

            // سجل التاريخ
            saveHistory(req, employeeId);
            requestRepo.save(req);

            // إشعار العميل
            notificationService.send(
                    req.getCustomer().getUser(),
                    "تم تحديث حالة طلبك رقم #" + req.getId()
            );

        }

    @Transactional
    public void updateStatus(
            Integer requestId,
            StaffRequestStatus status,
            Integer employeeId,
            String note,
            String imageUrl  // رابط الصورة المرفوعة
    ) {
        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (req.getAssignedEmployee() == null ||
                !employeeId.equals(req.getAssignedEmployee().getId())) {
            throw new RuntimeException("غير مصرح لك");
        }

        if(status == StaffRequestStatus.RECEIVED
                && (imageUrl == null || imageUrl.isBlank())) {

            throw new RuntimeException(
                    "يجب رفع صورة عند الاستلام"
            );
        }

        // تحديث الحالة
        req.setStaffStatus(status);
        req.setLastUpdated(LocalDateTime.now());

        // حفظ الصورة فقط إذا كانت الحالة RECEIVED
        if (status == StaffRequestStatus.RECEIVED && imageUrl != null) {
            req.setReceivedImageUrl(imageUrl);
        }


        updateStaffTimestamps(req, status);

        if (note != null && !note.isBlank()) {
            saveNote(req, employeeId, note);
        }

        saveHistory(req, employeeId);
        requestRepo.save(req);

        // إشعار العميل
        String msg = "تم تحديث حالة طلبك رقم #" + req.getId();
        if (status == StaffRequestStatus.RECEIVED && imageUrl != null) {
            msg += " وتم رفع صورة الاستلام.";
        }
        notificationService.send(req.getCustomer().getUser(), msg);
    }



    // =========================
        // حفظ الأوقات
        // =========================
        private void updateStaffTimestamps(
                CarServiceRequest req,
                StaffRequestStatus status
        ) {
            LocalDateTime now = LocalDateTime.now();

            switch (status) {

                case RECEIVED -> req.setReceivedAt(now);

                case INSPECTION_IN_PROGRESS -> req.setInspectionAt(now);

                case TESTING -> req.setTestingAt(now);

                case PRICING -> req.setPricingAt(now);

                case REPAIRING -> req.setRepairAt(now);

                case DELIVERED -> req.setDeliveredAt(now);
            }
        }



        // =========================
        // حفظ ملاحظة
        // =========================
        private void saveNote(
                CarServiceRequest req,
                Integer empId,
                String note
        ) {

            RequestNote n = new RequestNote();

            n.setRequest(req);
            n.setEmployeeId(empId);
            n.setNote(note);
            n.setCreatedAt(LocalDateTime.now());

            noteRepo.save(n);
        }


        // =========================
        // حفظ History
        // =========================
        private void saveHistory(
                CarServiceRequest req,
                Integer empId
        ) {

            RequestStatusHistory h = new RequestStatusHistory();

            h.setRequest(req);
            h.setStaffStatus(req.getStaffStatus());
            h.setCustomerStatus(req.getCustomerStatus());
            h.setPricingStatus(req.getPricingStatus());

            h.setChangedBy(empId);
            h.setChangedAt(LocalDateTime.now());

            historyRepo.save(h);
        }

    private EmployeeRequestResponseDto toEmployeeCardDto(CarServiceRequest r) {

        EmployeeRequestResponseDto dto = new EmployeeRequestResponseDto();

        dto.setId(r.getId());
        dto.setOrderNumber(r.getOrderNumber());

        // ✅ الحالة
        dto.setStatus(mapStageToArabic(r.getStage()));

        // ✅ نوع الخدمة (مرة وحدة فقط)
        dto.setServiceOption(
                r.getServiceOption() != null ? r.getServiceOption().name() : null
        );

        // ✅ بيانات السيارة (مؤقت)
        dto.setCarId(r.getCar().getId());

        // ✅ وصف المشكلة (مهم للموظف)
        dto.setProblemDescription(r.getProblemDescription());

        // ✅ الموقع
        if (r.getLocation() != null) {
            dto.setAddress(r.getLocation().getAddress());
        }

        return dto;
    }


    public String mapStaffStatus(StaffRequestStatus status) {
        return switch (status) {
            case NEW -> "جديدة";
            case RECEIVED -> "تم الاستلام";
            case INSPECTION_IN_PROGRESS -> "جاري الفحص";
            case TESTING -> "قيد التجربة";
            case REPORT_WRITING -> "إرفاق التقرير";
            case PARTS_REGISTERING -> "تسجيل القطع";
            case PRICING -> "جاري تسعير القطع";
            case REPAIRING -> "جاري الإصلاح";
            case DELIVERED -> "تم التسليم";
        };
    }

    private String mapStageToArabic(WorkflowStage stage) {

            if (stage == null) return "غير محدد";

            return switch (stage) {

                case NEW_REQUEST -> "طلب جديد";

                case ASSIGNED -> "تم الإسناد";
                case RECEIVED -> "تم الاستلام";
                case INSPECTION_IN_PROGRESS -> "قيد الفحص";
                case TESTING -> "تجربة";
                case REPORT_WRITING -> "كتابة التقرير";
                case PARTS_REGISTERING -> "تسجيل القطع";
                case PRICING -> "قيد التسعير";
                case WAITING_APPROVAL -> "بانتظار الموافقة";
                case REPAIRING -> "قيد الإصلاح";
                case READY -> "جاهز";
                case DELIVERED -> "تم التسليم";
                case CANCELLED -> "ملغي";
            };
        }
}
