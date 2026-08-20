package org.example.tears.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tears.Api.ApiException;
import org.example.tears.Model.MultipartInputStreamFileResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OcrService {

    private final String apiUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OcrService(@Value("${ocr.api-url:}") String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public Map<String, String> extractCarInfo(MultipartFile file) {
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new ApiException("OCR service is not configured. Set OCR_API_URL.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getSize()
            ));

            ResponseEntity<String> response = restTemplate().postForEntity(
                    apiUrl,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiException("OCR service returned HTTP " + response.getStatusCode().value());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            if (!root.path("success").asBoolean(false)) {
                throw new ApiException("OCR rejected the image: " + qualityIssues(root));
            }

            JsonNode data = root.path("data");
            if (data.isMissingNode() || data.isNull()) {
                throw new ApiException("OCR service returned no vehicle data.");
            }

            Map<String, String> result = new LinkedHashMap<>();
            result.put("plateNumberArabic", textOrNull(data, "plate_text_ar"));
            result.put("plateTextAr", textOrNull(data, "plate_text_ar"));
            result.put("plateNumberEnglish", textOrNull(data, "plate_text_en"));
            result.put("brandName", textOrNull(data, "vehicle_make"));
            result.put("modelName", textOrNull(data, "vehicle_model"));
            result.put("carYear", textOrNull(data, "model_year"));
            result.put("color", textOrNull(data, "color"));
            result.put("vin", textOrNull(data, "vin"));
            result.put("ownerName", textOrNull(data, "owner_name"));
            return result;
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw new ApiException("Could not read the uploaded image.", 400);
        } catch (Exception e) {
            throw new ApiException("OCR service request failed: " + e.getMessage(), 502);
        }
    }

    public String extractOwnerName(MultipartFile file) {
        return extractCarInfo(file).get("ownerName");
    }

    private RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);
        return new RestTemplate(factory);
    }

    private String qualityIssues(JsonNode root) {
        JsonNode issues = root.path("quality").path("issues");
        if (!issues.isArray() || issues.isEmpty()) {
            return "required vehicle fields were not found";
        }
        StringBuilder message = new StringBuilder();
        for (JsonNode issue : issues) {
            if (message.length() > 0) {
                message.append(", ");
            }
            message.append(issue.asText());
        }
        return message.toString();
    }

    private String textOrNull(JsonNode data, String field) {
        String value = data.path(field).asText("").trim();
        return value.isEmpty() ? null : value;
    }


}
