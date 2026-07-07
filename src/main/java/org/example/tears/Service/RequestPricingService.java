package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.PricingPartDto;
import org.example.tears.DTO.PricingRequestDto;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestPricingService {

    private final CarServiceRequestRepository requestRepo;
    private final RequestPartRepository partRepo;
    private final RequestStatusHistoryRepository historyRepo;
    private final RequestReportRepository reportRepo;
    private final RequestNoteRepository noteRepo;

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
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (request.getAssignedPricingEmployee() == null ||
                !request.getAssignedPricingEmployee().getId().equals(pricingEmployee.getId())) {

            throw new RuntimeException("الطلب غير مسند لك");
        }

        if (request.getPricingStatus() == PricingStatus.NEW) {

            request.setPricingStatus(PricingStatus.PRICING);
        }

        int totalPartsPrice = 0;
        int totalLabor = 0;

        for (PricingPartDto item : dto.getParts()) {

            RequestPart part = partRepo.findById(item.getPartId())
                    .orElseThrow(() -> new RuntimeException("القطعة غير موجودة"));

            if (!part.getRequest().getId().equals(requestId)) {
                throw new RuntimeException("القطعة لا تتبع هذا الطلب");
            }

            part.setFinalPrice(item.getFinalPrice());
            part.setPriced(true);

            partRepo.save(part);

            totalPartsPrice +=
                    item.getFinalPrice() * part.getQuantity();

            totalLabor += part.getLaborCost();
        }

        request.setFinalPrice(totalPartsPrice + totalLabor);

        request.setLastUpdated(LocalDateTime.now());

        List<RequestPart> parts =
                partRepo.findByRequestId(requestId);

        boolean allPriced =
                parts.stream()
                        .allMatch(RequestPart::getPriced);

        if (allPriced) {

            request.setPricingStatus(PricingStatus.PRICED);

            request.setLastUpdated(LocalDateTime.now());

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

                version = oldReport.getVersion() == null
                        ? 1
                        : oldReport.getVersion() + 1;
            }

            RequestReport report = new RequestReport();

            report.setRequest(request);
            report.setCreatedBy(pricingEmployee);

            report.setCreatedAt(LocalDateTime.now());

            report.setVersion(version);

            report.setLatest(true);

            report.setSent(false);

            reportRepo.save(report);
            request.setReportWrittenAt(LocalDateTime.now());
        }

        requestRepo.save(request);
    }

    public ResponseEntity<byte[]> downloadPricingReport(Integer requestId) throws Exception {

        // نتأكد أن التقرير موجود
        reportRepo.findByRequest_IdAndLatestTrue(requestId)
                .orElseThrow(() ->
                        new ApiException("التقرير غير موجود"));

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        RequestReport report =
                reportRepo.findByRequest_IdAndLatestTrue(requestId)
                        .orElseThrow(() ->
                                new ApiException("التقرير غير موجود"));

        List<RequestPart> parts =
                partRepo.findByRequestId(requestId);

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
                    part.getLaborCost();

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
                    part.getType(),
                    part.getQuantity(),
                    part.getProblemDescription(),
                    partPrice,
                    part.getLaborCost(),
                    total
            ));
        }

        String technicianNotes =
                notes.isEmpty()
                        ? "-"
                        : notes.get(0).getNote();
        String reportNumber =
                "PR-" + request.getOrderNumber() + "-V" + report.getVersion();


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
                        "attachment; filename=pricing-report-" +
                                request.getOrderNumber() +
                                ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(output.toByteArray());
    }

    @Transactional
    public void sendToTechnician(
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

        if (request.getPricingStatus() != PricingStatus.PRICED) {

            throw new ApiException("يجب حفظ التسعير أولاً");
        }

        RequestReport report =
                reportRepo.findByRequest_IdAndLatestTrue(requestId)
                        .orElseThrow(() ->
                                new ApiException("لا يوجد تقرير"));

        // إرسال التقرير
        report.setSent(true);
        reportRepo.save(report);

        // إعادة الطلب للفني
        request.setCurrentEmployee(request.getAssignedEmployee());

        // تغيير حالة الموظف
        request.setStaffStatus(StaffRequestStatus.REPORT_WRITING);

        request.setReportWrittenAt(LocalDateTime.now());

        request.setLastUpdated(LocalDateTime.now());

        requestRepo.save(request);
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