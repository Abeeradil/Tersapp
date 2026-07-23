package org.example.tears.Service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.*;
import org.example.tears.Enums.EmployeeRole;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Model.*;
import org.example.tears.Repository.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestPricingService {

    private final CarServiceRequestRepository requestRepo;
    private final RequestPartRepository partRepo;
    private final RequestStatusHistoryRepository historyRepo;
    private final NotificationService notificationService;
    private final RequestReportRepository reportRepo;
    private final RequestNoteRepository noteRepo;
    private final RequestApprovalRepository approvalRepo;

    @Transactional
    public void startPricing(Integer requestId, Employee employee){

        CarServiceRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ApiException("الطلب غير موجود"));

        if(request.getAssignedPricingEmployee() == null ||
                !request.getAssignedPricingEmployee().getId().equals(employee.getId())){
            throw new ApiException("الطلب غير مسند لك");
        }

        request.setPricingStatus(PricingStatus.PRICING);
        request.setPricingAt(LocalDateTime.now());

        requestRepo.save(request);
    }

    @Transactional
    public void pricingRequest(
            Integer requestId,
            PricingRequestDto dto,
            Employee pricingEmployee
    ) {

        CarServiceRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ApiException("الطلب غير موجود"));

        if (request.getAssignedPricingEmployee() == null ||
                !request.getAssignedPricingEmployee().getId().equals(pricingEmployee.getId())) {

            throw new ApiException("الطلب غير مسند لك");
        }

        if (request.getPricingStatus() != PricingStatus.PRICING) {
            throw new ApiException("يجب بدء التسعير أولاً");
        }

        int totalPartsPrice = 0;
        int totalLabor = 0;

        for (PricingPartDto item : dto.getParts()) {

            RequestPart part = partRepo.findById(item.getPartId())
                    .orElseThrow(() -> new ApiException("القطعة غير موجودة"));

            if (!part.getRequest().getId().equals(requestId)) {
                throw new ApiException("القطعة لا تتبع هذا الطلب");
            }

            part.setFinalPrice(item.getFinalPrice());
            part.setPriced(true);

            partRepo.save(part);

            totalPartsPrice += item.getFinalPrice() * part.getQuantity();
            totalLabor += part.getLaborCost();
        }

        request.setFinalPrice(totalPartsPrice + totalLabor);

        request.setLastUpdated(LocalDateTime.now());

        requestRepo.save(request);
    }

    @Transactional
    public void finishPricing(
            Integer requestId,
            Employee pricingEmployee
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (request.getAssignedPricingEmployee() == null ||
                !request.getAssignedPricingEmployee().getId().equals(pricingEmployee.getId())) {

            throw new ApiException("الطلب غير مسند لك");
        }

        if (request.getPricingStatus() != PricingStatus.PRICING) {
            throw new ApiException("الطلب ليس في مرحلة التسعير");
        }

        List<RequestPart> parts =
                partRepo.findByRequestId(requestId);

        boolean allPriced =
                parts.stream().allMatch(RequestPart::getPriced);

        if (!allPriced) {
            throw new ApiException("يجب تسعير جميع القطع أولاً");
        }

        request.setPricingStatus(PricingStatus.PRICED);

        saveHistory(
                request,
                pricingEmployee.getId()
        );

        RequestReport oldReport =
                reportRepo.findByRequest_IdAndLatestTrue(requestId)
                        .orElse(null);

        int version = 1;

        if (oldReport != null) {

            oldReport.setLatest(false);

            reportRepo.save(oldReport);

            version = oldReport.getVersion() + 1;
        }

        RequestReport report = new RequestReport();

        report.setRequest(request);
        report.setCreatedBy(pricingEmployee);
        report.setCreatedAt(LocalDateTime.now());
        report.setVersion(version);
        report.setLatest(true);
        report.setSent(false);

// أول حفظ للحصول على الـ id
        reportRepo.save(report);

// الآن الـ id أصبح موجودًا
        report.setReportNumber(
                "PR-" + String.format("%06d", report.getId())
        );

// تحديث التقرير برقم التقرير
        reportRepo.save(report);

// نسخ القطع بعد اكتمال التقرير
        clonePartsToReport(request, report);

        request.setCurrentEmployee(
                request.getAssignedEmployee()
        );

        request.setStaffStatus(
                StaffRequestStatus.REPORT_WRITING
        );

        request.setReportWrittenAt(LocalDateTime.now());

        request.setLastUpdated(LocalDateTime.now());

        requestRepo.save(request);
        RequestApproval approval =
                approvalRepo.findByRequest_Id(requestId)
                        .orElse(null);

        if (approval == null) {

            approval = new RequestApproval();
            approval.setRequest(request);

        }

        approval.setApproved(null);
        approval.setDecisionAt(null);
        approval.setCustomerNote(null);

        approvalRepo.save(approval);


        notificationService.send(
                request.getAssignedEmployee().getUser(),
                "تم الانتهاء من التسعير للطلب #" +
                        request.getOrderNumber()
        );
    }

    public ResponseEntity<byte[]> downloadPricingReport(
            Integer requestId,
            Employee employee
    ) throws Exception {

        RequestReport report =
                getAccessibleReport(requestId, employee);

        return generatePdf(report);
    }


    public ResponseEntity<byte[]> generatePdf(
            RequestReport report
    ) throws Exception {

        CarServiceRequest request =
                report.getRequest();

        List<RequestPart> parts =
                partRepo.findByReport_Id(report.getId());

        List<RequestNote> notes =
                noteRepo.findByRequestOrderByCreatedAtDesc(request);

        StringBuilder rows = new StringBuilder();

        int grandTotal = 0;
        int totalPartsPrice = 0;
        int totalLabor = 0;



        for (RequestPart part : parts) {

            int partPrice = part.getFinalPrice() == null ? 0 : part.getFinalPrice();
            int partsCost =
                    partPrice * part.getQuantity();

            int labor =
                    part.getLaborCost()== null ? 0 : part.getLaborCost();

            int total =
                    partsCost + labor;

            totalPartsPrice += partsCost;
            totalLabor += labor;
            grandTotal += total;


            rows.append("""
                            <tr>
                            <td>%s</td>
                            <td>%s</td>
                            <td>%d</td>
                            <td>%s</td>
                            <td>%d SAR</td>
                            <td>%d SAR</td>
                            <td>%d SAR</td>
                            </tr>
                            """.formatted(
                    part.getName(),
                    part.getType() == null ? "-" : part.getType(),
                    part.getQuantity(),
                    part.getProblemDescription() == null ? "-" : part.getProblemDescription(),
                    partPrice,
                    labor,
                    total
            ));
        }

        String technicianNotes =
                notes.isEmpty()
                        ? "-"
                        : notes.get(0).getNote();
        String reportNumber =
                report.getReportNumber();


        ClassPathResource resource =
                new ClassPathResource("templates/pricing-report.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        html = html.replace("${customerName}",
                request.getCustomer().getUser().getFullName());

        html = html.replace("${orderNumber}",
                request.getOrderNumber());
        html = html.replace(
                "${reportNumber}",
                reportNumber
        );

        html = html.replace("${carModel}",
                request.getCar().getModel().getName());

        html = html.replace("${serviceOption}",
                request.getServiceOption().name());

        html = html.replace("${phone}",
                request.getCustomer().getUser().getPhoneNumber());

        html = html.replace("${problem}",
                request.getProblemDescription());

        html = html.replace("${technicianNotes}",
                technicianNotes);

        html = html.replace("${rows}",
                rows.toString());

        html = html.replace("${partsTotal}",
                String.valueOf(totalPartsPrice));

        html = html.replace("${laborTotal}",
                String.valueOf(totalLabor));

        html = html.replace("${vat}",
                String.valueOf(request.getVatAmount()));

        html = html.replace("${discount}",
                String.valueOf(request.getDiscount()));

        html = html.replace("${grandTotal}",
                String.valueOf(grandTotal));

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfRendererBuilder builder =
                new PdfRendererBuilder();

        String baseUrl = new ClassPathResource("").getURL().toExternalForm();

        ClassPathResource font =
                new ClassPathResource("fonts/Cairo-Regular.ttf");

        builder.useFont(
                () -> {
                    try {
                        return font.getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                "Cairo",
                400,
                PdfRendererBuilder.FontStyle.NORMAL,
                true
        );

        builder.withHtmlContent(html, baseUrl);

        builder.toStream(output);

        builder.run();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pricing-report-"+report.getReportNumber()+".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(output.toByteArray());

    }


    @Transactional(readOnly = true)
    public ReportPreviewDto getEmpReport(
            Integer requestId,
            Employee employee
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        List<RequestPart> parts;

        if (employee.getEmployeeRole() == EmployeeRole.PRICING) {

            RequestReport report =
                    getAccessibleReport(requestId, employee);

            parts = partRepo.findByReport_Id(report.getId());

        } else {

            parts = partRepo.findByRequestId(requestId);

        }

        ReportPreviewDto dto = new ReportPreviewDto();

        dto.setRequestId(request.getId());
        dto.setOrderNumber(request.getOrderNumber());
        dto.setCustomerName(request.getCustomer().getUser().getFullName());
        dto.setCarModel(request.getCar().getModel().getName());
        dto.setProblemDescription(request.getProblemDescription());

        approvalRepo.findByRequest_Id(requestId)
                .ifPresent(a -> dto.setCustomerApproved(a.getApproved()));

        List<CustomerReportPartDto> list = new ArrayList<>();

        int totalPartsPrice = 0;
        int totalLabor = 0;
        double grandTotal = 0;

        for (RequestPart part : parts) {

            CustomerReportPartDto p = new CustomerReportPartDto();

            p.setPartId(part.getId());
            p.setName(part.getName());
            p.setQuantity(part.getQuantity());
            p.setFinalPrice(part.getFinalPrice());
            p.setLaborCost(part.getLaborCost());

            int partPrice =
                    part.getFinalPrice() == null ? 0 : part.getFinalPrice();

            int labor =
                    part.getLaborCost() == null ? 0 : part.getLaborCost();

            int partsCost =
                    partPrice * part.getQuantity();

            int total =
                    partsCost + labor;

            p.setTotal((double) total);

            list.add(p);

            totalPartsPrice += partsCost;
            totalLabor += labor;

            grandTotal += total;
        }

        dto.setParts(list);
        dto.setTotalPartsPrice(totalPartsPrice);
        dto.setTotalLabor(totalLabor);
        dto.setGrandTotal(grandTotal);

        return dto;
    }

    private void clonePartsToReport(
            CarServiceRequest request,
            RequestReport report
    ) {

        List<RequestPart> currentParts =
                partRepo.findByRequestId(request.getId());

        for (RequestPart oldPart : currentParts) {

            RequestPart copy = new RequestPart();

            copy.setRequest(request);

            copy.setReport(report);

            copy.setName(oldPart.getName());

            copy.setType(oldPart.getType());

            copy.setQuantity(oldPart.getQuantity());

            copy.setProblemDescription(oldPart.getProblemDescription());

            copy.setFinalPrice(oldPart.getFinalPrice());

            copy.setLaborCost(oldPart.getLaborCost());

            copy.setPriced(oldPart.getPriced());

            partRepo.save(copy);
        }
    }

    private RequestReport getAccessibleReport(
            Integer requestId,
            Employee employee
    ) {

        if (employee.getEmployeeRole() == EmployeeRole.PRICING) {

            return reportRepo
                    .findTopByRequest_IdAndCreatedBy_IdOrderByVersionDesc(
                            requestId,
                            employee.getId()
                    )
                    .orElseThrow(() ->
                            new ApiException("لا يوجد تقرير لهذا الموظف"));

        }

        return reportRepo
                .findByRequest_IdAndLatestTrue(requestId)
                .orElseThrow(() ->
                        new ApiException("لا يوجد تقرير"));
    }


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

}