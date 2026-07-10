package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.PricingRequestCardDto;
import org.example.tears.DTO.PricingRequestDetailsDto;
import org.example.tears.DTO.PricingTimelineDto;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Mapper.PricingRequestMapper;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestReport;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestReportRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PricingQueryService {

    private final CarServiceRequestRepository requestRepo;
    private final PricingRequestMapper pricingRequestMapper;
    private final RequestReportRepository reportRepo;


    public List<PricingRequestCardDto> getMyRequests(Employee employee) {

        return requestRepo.findByAssignedPricingEmployee(employee)
                .stream()
                .map(pricingRequestMapper::toPricingCardDto)
                .toList();
    }


    public PricingRequestDetailsDto getRequestDetails(
            Integer requestId,
            Employee employee
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (request.getAssignedPricingEmployee() == null ||
                !request.getAssignedPricingEmployee().getId().equals(employee.getId())) {

            throw new ApiException("غير مصرح لك");
        }
        PricingRequestDetailsDto dto =
                pricingRequestMapper.toPricingDetailsDto(request);


        RequestReport report =
                reportRepo.findByRequest_IdAndLatestTrue(request.getId())
                        .orElse(null);

        dto.setTimeline(buildPricingTimeline(request, report));

        if(report != null){

            dto.setReportVersion(report.getVersion());

            dto.setReportReady(true);

            dto.setReportNumber(
                    "PR-" +
                            request.getOrderNumber() +
                            "-V" +
                            report.getVersion()
            );

            dto.setReportSent(report.isSent());

        }


        return dto;
    }

    private List<PricingTimelineDto> buildPricingTimeline(
            CarServiceRequest request,
            RequestReport report
    ) {

        List<PricingTimelineDto> list = new ArrayList<>();

        list.add(new PricingTimelineDto(
                "New",
                PricingStatus.NEW,
                request.getLastUpdated(),
                request.getPricingStatus().ordinal() >= PricingStatus.NEW.ordinal(),
                request.getPricingStatus() == PricingStatus.NEW
        ));

        list.add(new PricingTimelineDto(
                "Pricing",
                PricingStatus.PRICING,
                request.getPricingAt(),
                request.getPricingStatus().ordinal() >= PricingStatus.PRICING.ordinal(),
                request.getPricingStatus() == PricingStatus.PRICING
        ));

        list.add(new PricingTimelineDto(
                "Priced",
                PricingStatus.PRICED,
                request.getLastUpdated(),
                request.getPricingStatus() == PricingStatus.PRICED,
                request.getPricingStatus() == PricingStatus.PRICED
        ));

        list.add(new PricingTimelineDto(
                "Report Generated",
                PricingStatus.PRICED,
                report == null ? null : report.getCreatedAt(),
                report != null,
                false
        ));

        list.add(new PricingTimelineDto(
                "Sent To Technician",
                PricingStatus.PRICED,
                report != null && report.isSent()
                        ? request.getLastUpdated()
                        : null,
                report != null && report.isSent(),
                false
        ));

        return list;
    }


}
