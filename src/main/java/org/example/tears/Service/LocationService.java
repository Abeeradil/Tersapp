package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.InpDTO.LocationDto;
import org.example.tears.Model.Location;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.OutLocationDto;
import org.example.tears.Repository.LocationRepository;
import org.springframework.stereotype.Service;

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

        return out;
    }
}