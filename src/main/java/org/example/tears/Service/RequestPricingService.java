package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.PricingPartDto;
import org.example.tears.DTO.PricingRequestDto;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestPart;
import org.example.tears.Model.RequestReport;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.example.tears.Repository.RequestReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestPricingService {

    private final CarServiceRequestRepository requestRepo;
    private final RequestPartRepository partRepo;
    private final RequestReportRepository reportRepo;

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
}