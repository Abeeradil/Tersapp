package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.PricingPartDto;
import org.example.tears.DTO.PricingRequestDto;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Model.*;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestNoteRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.example.tears.Repository.RequestReportRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestPricingService {

    private final CarServiceRequestRepository requestRepo;
    private final RequestPartRepository partRepo;
    private final RequestReportRepository reportRepo;
    private final RequestNoteRepository noteRepo;


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

            reportRepo.save(report);
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

        List<RequestPart> parts =
                partRepo.findByRequestId(requestId);

        List<RequestNote> notes =
                noteRepo.findByRequestOrderByCreatedAtDesc(request);

        StringBuilder rows = new StringBuilder();

        int grandTotal = 0;

        for (RequestPart part : parts) {

            int partPrice = part.getFinalPrice() == null ? 0 : part.getFinalPrice();

            int total =
                    (partPrice * part.getQuantity()) +
                            part.getLaborCost();

            grandTotal += total;

            rows.append("""
                <tr>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%d</td>
                    <td>%d</td>
                    <td>%d</td>
                </tr>
                """.formatted(
                    part.getName(),
                    part.getQuantity(),
                    partPrice,
                    part.getLaborCost(),
                    total
            ));
        }

        String technicianNotes =
                notes.isEmpty()
                        ? "-"
                        : notes.get(0).getNote();

        ClassPathResource resource =
                new ClassPathResource("templates/pricing-report.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        html = html.replace("${orderNumber}",
                request.getOrderNumber());

        html = html.replace("${customerName}",
                request.getCustomer().getUser().getFullName());

        html = html.replace("${carModel}",
                request.getCar().getModel().getName());

        html = html.replace("${problem}",
                request.getProblemDescription());

        html = html.replace("${technicianNotes}",
                technicianNotes);

        html = html.replace("${rows}",
                rows.toString());

        html = html.replace("${grandTotal}",
                String.valueOf(grandTotal));

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfRendererBuilder builder =
                new PdfRendererBuilder();

        String baseUrl = new ClassPathResource("").getURL().toExternalForm();

        builder.withHtmlContent(html, baseUrl);

        builder.withHtmlContent(html, null);

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

}