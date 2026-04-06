package org.example.tears.DTO;

import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.InpDTO.LocationDto;
import org.example.tears.Model.Location;
import org.example.tears.Model.User;
import org.example.tears.Repository.LocationRepository;
import org.springframework.stereotype.Service;

@Service
public class RequestMapper {

    private final LocationRepository locationRepository;

    public RequestMapper(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location resolveLocation(User user, CreateRequestStepDto dto) {
        if (dto.getLocationId() != null) {
            Location loc = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new RuntimeException("الموقع غير موجود"));
            if (!loc.getCustomer().getId().equals(user.getCustomer().getId()))
                throw new RuntimeException("الموقع لا يخص المستخدم");
            return loc;
        }

        if (dto.getNewLocation() != null) {
            LocationDto locDto = dto.getNewLocation();
            boolean valid = isInMakkah(locDto.getLat(), locDto.getLng()) ||
                    isInJeddah(locDto.getLat(), locDto.getLng());
            if (!valid) throw new RuntimeException("الخدمة متاحة فقط داخل مكة أو جدة");

            Location loc = new Location();
            loc.setLat(locDto.getLat());
            loc.setLng(locDto.getLng());
            loc.setAddress(locDto.getAddress());
            loc.setCustomer(user.getCustomer());
            return locationRepository.save(loc);
        }

        throw new RuntimeException("يجب اختيار أو إضافة موقع");
    }

    private boolean isInMakkah(double lat, double lng) {
        return lat >= 21.25 && lat <= 21.55 && lng >= 39.70 && lng <= 40.05;
    }

    private boolean isInJeddah(double lat, double lng) {
        return lat >= 21.45 && lat <= 21.75 && lng >= 39.05 && lng <= 39.35;
    }
}
