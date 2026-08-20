package org.example.tears.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tears.DTO.IstimaraData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class OpenAiIstimaraService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-5.6-luna}")
    private String model;

    @Value("${openai.url:https://api.openai.com/v1/responses}")
    private String openAiUrl;

    public OpenAiIstimaraService(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }


    // =========================================================
    // Main method
    // =========================================================

    public IstimaraData extractIstimara(
            MultipartFile file
    ) {

        try {

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException(
                        "Istimara image is empty."
                );
            }

            String contentType = file.getContentType();

            if (contentType == null) {
                throw new IllegalArgumentException(
                        "Image content type is missing."
                );
            }

            if (!Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            ).contains(contentType)) {

                throw new IllegalArgumentException(
                        "Only JPEG, PNG and WEBP images are supported."
                );
            }


            // =================================================
            // Convert image to Base64
            // =================================================

            byte[] imageBytes = file.getBytes();

            String base64Image =
                    Base64.getEncoder()
                            .encodeToString(imageBytes);


            String imageDataUrl =
                    "data:" +
                            contentType +
                            ";base64," +
                            base64Image;


            // =================================================
            // Build OpenAI request
            // =================================================

            Map<String, Object> request =
                    buildRequest(imageDataUrl);


            String requestJson =
                    objectMapper.writeValueAsString(
                            request
                    );


            // =================================================
            // HTTP headers
            // =================================================

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            headers.setBearerAuth(
                    apiKey
            );


            HttpEntity<String> entity =
                    new HttpEntity<>(
                            requestJson,
                            headers
                    );


            // =================================================
            // Send request
            // =================================================

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            openAiUrl,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            System.out.println("======================================");
            System.out.println("OPENAI STATUS: " + response.getStatusCode());
            System.out.println("OPENAI RESPONSE:");
            System.out.println(response.getBody());
            System.out.println("======================================");


            if (!response.getStatusCode().is2xxSuccessful()) {

                throw new RuntimeException(
                        "OpenAI returned HTTP " +
                                response.getStatusCodeValue() +
                                ": " +
                                response.getBody()
                );
            }


            // =================================================
            // Extract structured output
            // =================================================

            String json =
                    extractOutputJson(
                            response.getBody()
                    );


            return objectMapper.readValue(
                    json,
                    IstimaraData.class
            );

        }

        catch (Exception e) {

            throw new RuntimeException(
                    "Failed to extract Istimara data: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // =========================================================
    // Build request
    // =========================================================

    private Map<String, Object> buildRequest(
            String imageDataUrl
    ) {

        Map<String, Object> request =
                new LinkedHashMap<>();


        request.put(
                "model",
                model
        );


        // =====================================================
        // Instructions
        // =====================================================

        request.put(
                "instructions",
                """
                You are an expert document extraction system.
        
                You are analyzing a Saudi Arabian vehicle
                registration document (Istimara).
        
                The document may contain BOTH Arabic and English.
        
                Your job is to carefully inspect the entire image
                and extract vehicle information.
        
                IMPORTANT RULES:
        
                1. Read Arabic and English text.
        
                2. Use the labels and the physical position of
                   fields on the document to identify values.
        
                3. Do NOT confuse the plate number with a document
                   number, serial number, registration number,
                   transaction number, or any other number.
        
                4. Do NOT confuse the VIN/chassis number with
                   the plate number.
        
        
                IMPORTANT PLATE READING:
        
                Saudi vehicle plates normally contain three Arabic
                letters and four Arabic digits.
        
                - Carefully inspect the entire plate area before
                  returning the result.
        
                - Read ALL visible Arabic letters on the plate.
        
                - Do NOT stop after reading two letters.
        
                - Pay special attention to the middle letter.
        
                - If all three Arabic letters are visible,
                  plate_text_ar MUST contain all three letters.
        
                - If all three corresponding English letters can
                  be reliably determined, plate_text_en MUST
                  contain all three letters.
        
                - Do not omit a middle letter.
        
                - Compare the Arabic and English representations
                  of the SAME plate before returning the result.
        
                - If a letter is clearly visible in one representation
                  and its corresponding character can be reliably
                  determined from the same plate, use that information
                  to complete the other representation.
        
                - Do NOT invent a character that is not visible or
                  reliably determinable.
        
                - If one character is genuinely unreadable and cannot
                  be reliably determined from the image or the
                  corresponding representation, return null rather
                  than guessing.
        
        
                VIN:
        
                5. The VIN normally contains exactly 17 characters.
        
                6. If a 17-character VIN is visible, return it
                   exactly as printed.
        
                7. Do not confuse the VIN/chassis number with
                   the plate number.
        
        
                VEHICLE INFORMATION:
        
                8. Identify the vehicle make from the actual
                   vehicle make field.
        
                9. Identify the vehicle model from the actual
                   vehicle model field.
        
                10. Identify the model year from the actual
                    manufacturing/model-year field.
        
                11. Identify the vehicle color from the actual
                    color field.
        
                12. Identify the owner name from the actual
                    owner field.
        
        
                GENERAL RULES:
        
                13. Do not invent information.
        
                14. If a field is not visible or cannot be read
                    reliably, return null.
        
                15. Do not use general knowledge to guess missing
                    values.
        
                16. Carefully inspect small Arabic text.
        
                17. The image may be low resolution. Use the
                    surrounding labels and document layout to
                    identify the correct fields.
        
                18. Return ONLY the requested JSON structure.
                """
        );

        // =====================================================
        // Input
        // =====================================================

        List<Map<String, Object>> content =
                new ArrayList<>();


        Map<String, Object> text =
                new LinkedHashMap<>();

        text.put(
                "type",
                "input_text"
        );

        text.put(
                "text",
                """
                Extract the following fields from this Saudi
                vehicle registration document:

                - plate_number
                - plate_text_ar
                - plate_text_en
                - vehicle_make
                - vehicle_model
                - model_year
                - color
                - vin
                - owner_name

                Be especially careful with Arabic fields.
                Do not guess.
                """
        );


        content.add(text);


        Map<String, Object> image =
                new LinkedHashMap<>();

        image.put(
                "type",
                "input_image"
        );

        image.put(
                "image_url",
                imageDataUrl
        );

        // High detail is important for small Arabic fields.
        image.put(
                "detail",
                "high"
        );


        content.add(image);


        Map<String, Object> userMessage =
                new LinkedHashMap<>();

        userMessage.put(
                "role",
                "user"
        );

        userMessage.put(
                "content",
                content
        );


        request.put(
                "input",
                List.of(userMessage)
        );


        // =====================================================
        // Structured Outputs
        // =====================================================

        Map<String, Object> schema =
                new LinkedHashMap<>();

        schema.put(
                "type",
                "object"
        );


        Map<String, Object> properties =
                new LinkedHashMap<>();


        properties.put(
                "plate_number",
                nullableString()
        );

        properties.put(
                "plate_text_ar",
                nullableString()
        );

        properties.put(
                "plate_text_en",
                nullableString()
        );

        properties.put(
                "vehicle_make",
                nullableString()
        );

        properties.put(
                "vehicle_model",
                nullableString()
        );

        properties.put(
                "model_year",
                nullableString()
        );

        properties.put(
                "color",
                nullableString()
        );

        properties.put(
                "vin",
                nullableString()
        );

        properties.put(
                "owner_name",
                nullableString()
        );


        schema.put(
                "properties",
                properties
        );


        // OpenAI Structured Outputs requires all
        // properties to be listed as required.
        schema.put(
                "required",
                List.of(
                        "plate_number",
                        "plate_text_ar",
                        "plate_text_en",
                        "vehicle_make",
                        "vehicle_model",
                        "model_year",
                        "color",
                        "vin",
                        "owner_name"
                )
        );


        schema.put(
                "additionalProperties",
                false
        );


        Map<String, Object> format =
                new LinkedHashMap<>();

        format.put(
                "type",
                "json_schema"
        );

        format.put(
                "name",
                "saudi_istimara"
        );

        format.put(
                "strict",
                true
        );

        format.put(
                "schema",
                schema
        );


        Map<String, Object> textConfig =
                new LinkedHashMap<>();

        textConfig.put(
                "format",
                format
        );


        request.put(
                "text",
                textConfig
        );


        return request;
    }


    // =========================================================
    // Nullable string schema
    // =========================================================

    private Map<String, Object> nullableString() {

        Map<String, Object> schema =
                new LinkedHashMap<>();

        schema.put(
                "type",
                List.of(
                        "string",
                        "null"
                )
        );

        return schema;
    }


    // =========================================================
    // Extract JSON from Responses API
    // =========================================================

    private String extractOutputJson(
            String responseBody
    ) throws Exception {

        JsonNode root =
                objectMapper.readTree(
                        responseBody
                );


        // =====================================================
        // Normal Responses API output:
        //
        // output[]
        //   -> message
        //      -> content[]
        //         -> output_text
        //            -> text
        // =====================================================

        JsonNode output =
                root.path("output");


        if (!output.isArray()) {

            throw new RuntimeException(
                    "OpenAI response does not contain output."
            );
        }


        for (JsonNode outputItem : output) {

            JsonNode content =
                    outputItem.path(
                            "content"
                    );


            if (!content.isArray()) {
                continue;
            }


            for (JsonNode contentItem : content) {

                String type =
                        contentItem
                                .path("type")
                                .asText();


                if ("output_text".equals(type)) {

                    String text =
                            contentItem
                                    .path("text")
                                    .asText();


                    if (text != null
                            && !text.isBlank()) {

                        return text;
                    }
                }
            }
        }


        throw new RuntimeException(
                "Could not find structured JSON in OpenAI response."
        );
    }
    public String testConnection() {

        try {

            HttpHeaders headers = new HttpHeaders();

            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new LinkedHashMap<>();

            request.put("model", model);

            request.put(
                    "input",
                    "Reply with exactly: OPENAI_CONNECTION_OK"
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(
                            request,
                            headers
                    );

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            openAiUrl,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            System.out.println("=================================");
            System.out.println("OPENAI TEST STATUS:");
            System.out.println(response.getStatusCode());
            System.out.println("OPENAI TEST RESPONSE:");
            System.out.println(response.getBody());
            System.out.println("=================================");

            return response.getBody();

        } catch (Exception e) {

            System.out.println("=================================");
            System.out.println("OPENAI CONNECTION ERROR");
            System.out.println(e.getMessage());
            System.out.println("=================================");

            throw new RuntimeException(
                    "OpenAI connection failed: " + e.getMessage(),
                    e
            );
        }
    }
}