package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestPartService {

    private final RequestPartRepository requestPartRepository;

    public void setFinalPrice(Integer partId, Integer price) {

        RequestPart part = requestPartRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part not found"));

        part.setFinalPrice(price);

        requestPartRepository.save(part);
    }
}