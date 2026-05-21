package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.InpDTO.LocationDto;
import org.example.tears.Model.Location;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.OutLocationDto;
import org.example.tears.Repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final AuthService authService;

    public OutLocationDto addLocation(
            HttpServletRequest request,
            LocationDto dto
    ) {

        User user = authService.getAuthenticatedUser(request);

        if (dto.getLat() == null || dto.getLng() == null) {
            throw new ApiException("الإحداثيات مطلوبة");
        }

        if (dto.getAddress() == null || dto.getAddress().isBlank()) {
            throw new ApiException("العنوان مطلوب");
        }

        Location location = new Location();

        location.setLat(dto.getLat());
        location.setLng(dto.getLng());

        location.setAddress(dto.getAddress());
        location.setTitle(dto.getTitle());

        location.setCustomer(user.getCustomer());

        Location saved = locationRepository.save(location);

        OutLocationDto out = new OutLocationDto();

        out.setId(saved.getId());
        out.setLat(saved.getLat());
        out.setLng(saved.getLng());

        out.setAddress(saved.getAddress());
        out.setTitle(saved.getTitle());

        return mapToDto(saved);

    }

    // ================= UPDATE LOCATION =================
    public OutLocationDto updateLocation(
            HttpServletRequest request,
            Integer locationId,
            LocationDto dto
    ) {

        User user = authService.getAuthenticatedUser(request);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ApiException("الموقع غير موجود"));

        // ownership check
        if (!location.getCustomer().getId()
                .equals(user.getCustomer().getId())) {
            throw new ApiException("الموقع لا يخص المستخدم");
        }

        // partial update (PATCH behavior)
        if (dto.getLat() != null) location.setLat(dto.getLat());
        if (dto.getLng() != null) location.setLng(dto.getLng());
        if (dto.getAddress() != null) location.setAddress(dto.getAddress());
        if (dto.getTitle() != null) location.setTitle(dto.getTitle());

        Location saved = locationRepository.save(location);

        return mapToDto(saved);
    }


    private OutLocationDto mapToDto(Location location) {

        OutLocationDto dto = new OutLocationDto();

        dto.setId(location.getId());
        dto.setLat(location.getLat());
        dto.setLng(location.getLng());
        dto.setAddress(location.getAddress());
        dto.setTitle(location.getTitle());

        return dto;
    }
    // ================= DELETE LOCATION =================
    public void deleteLocation(
            HttpServletRequest request,
            Integer locationId
    ) {

        User user = authService.getAuthenticatedUser(request);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ApiException("الموقع غير موجود"));

        if (!location.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new ApiException("الموقع لا يخص المستخدم");
        }

        locationRepository.delete(location);
    }
        // ---------------------------
        // Resolve Location
        // ---------------------------
        public Location resolveLocation(CreateRequestStepDto dto, User user) {

            Location location;

            if (dto.getLocationId() != null) {

                location = locationRepository.findById(dto.getLocationId())
                        .orElseThrow(() -> new RuntimeException("الموقع غير موجود"));

                if (!location.getCustomer().getId().equals(user.getCustomer().getId())) {
                    throw new RuntimeException("الموقع لا يخص المستخدم");
                }

            } else if (dto.getNewLocation() != null) {

                LocationDto loc = dto.getNewLocation();

                validateSupportedCity(loc.getLat(), loc.getLng());

                location = createAndSaveLocation(loc, user);

            } else if (dto.getLocations() != null && !dto.getLocations().isEmpty()) {

                LocationDto loc = dto.getLocations().get(0);

                validateSupportedCity(loc.getLat(), loc.getLng());

                location = createAndSaveLocation(loc, user);

            } else {

                throw new RuntimeException("يجب اختيار أو إضافة موقع");
            }

            return location;
        }

        // ---------------------------
        // Create Location
        // ---------------------------
        private Location createAndSaveLocation(LocationDto loc, User user) {

            Location location = new Location();

            location.setLat(loc.getLat());
            location.setLng(loc.getLng());
            location.setAddress(loc.getAddress());
            location.setTitle(loc.getTitle());

            location.setCustomer(user.getCustomer());

            return locationRepository.save(location);
        }

        // ---------------------------
        // My Locations
        // ---------------------------
        public List<OutLocationDto> getMyLocations(HttpServletRequest request) {

            User user = authService.getAuthenticatedUser(request);

            return locationRepository
                    .findByCustomerId(user.getCustomer().getId())
                    .stream()
                    .map(this::mapToDto)
                    .toList();
        }

        // ---------------------------
        // Supported Cities
        // ---------------------------
        private void validateSupportedCity(double lat, double lng) {

            if (!isInMakkah(lat, lng) && !isInJeddah(lat, lng)) {
                throw new RuntimeException("الخدمة متاحة فقط داخل مكة أو جدة");
            }
        }

        private boolean isInMakkah(double lat, double lng) {

            return lat >= 21.25 && lat <= 21.55
                    && lng >= 39.70 && lng <= 40.05;
        }

        private boolean isInJeddah(double lat, double lng) {

            return lat >= 21.45 && lat <= 21.75
                    && lng >= 39.05 && lng <= 39.35;
        }

}