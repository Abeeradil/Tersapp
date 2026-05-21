package org.example.tears.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Model.MultipartInputStreamFileResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OcrService {

    public Map<String, String> extractCarInfo(MultipartFile file) {

        try {

            String apiUrl = "http://127.0.0.1:8000/extract-istimara";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("file",
                    new MultipartInputStreamFileResource(
                            file.getInputStream(),
                            file.getOriginalFilename()
                    )
            );

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response =
                    restTemplate.postForEntity(apiUrl, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if (!root.path("success").asBoolean(false)) {
                throw new ApiException("OCR failed");
            }

            JsonNode data = root.path("data");

            Map<String, String> result = new LinkedHashMap<>();

            result.put("plateNumberArabic", data.path("plate_number").asText(null));
            result.put("plateTextAr", data.path("plate_text_ar").asText(null));

            result.put("plateNumberEnglish", data.path("plate_text_en").asText(null));

            result.put("brandName", data.path("vehicle_make").asText(null));
            result.put("modelName", data.path("vehicle_model").asText(null));

            result.put("carYear", data.path("model_year").asText(null));
            result.put("color", data.path("color").asText(null));

            result.put("vin", data.path("vin").asText(null));
            result.put("ownerName", data.path("owner_name").asText(null));

            return result;

        } catch (Exception e) {
            throw new ApiException("OCR API failed: " + e.getMessage());
        }
    }
    public String extractOwnerName(MultipartFile file) {
        Map<String, String> info = extractCarInfo(file);
        return info.get("ownerName");
    }

    public String extractTextFromImage(MultipartFile file) {
        throw new UnsupportedOperationException("Optional feature (Tesseract)");
    }
}