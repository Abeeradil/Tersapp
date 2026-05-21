package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.InpDTO.LocationDto;
import org.example.tears.Service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/tears/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/add")
    public ResponseEntity<?> addLocation(
            HttpServletRequest request,
            @RequestBody LocationDto dto
    ) {

        return ResponseEntity.ok(
                locationService.addLocation(request, dto)
        );
    }
    @PostMapping("/update/{locationId}")
    public ResponseEntity<?> updateLocation(
            HttpServletRequest request,
            @PathVariable Integer locationId,
            @RequestBody LocationDto dto
    ) {

        return ResponseEntity.ok(
                locationService.updateLocation(request,locationId,dto)
        );
    }
    @DeleteMapping("/delete/{locationId}")
    public ResponseEntity<ApiResponse> deleteLocation(
            HttpServletRequest request,
            @PathVariable Integer locationId
    ) {

        locationService.deleteLocation(request, locationId);

        return ResponseEntity.ok(
                new ApiResponse(true, "تم حذف الموقع بنجاح")
        );
    }
}