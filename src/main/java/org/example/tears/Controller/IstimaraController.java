package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.IstimaraData;
import org.example.tears.DTO.IstimaraResponse;
import org.example.tears.DTO.Quality;
import org.example.tears.Service.OpenAiIstimaraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class IstimaraController {

    private final OpenAiIstimaraService openAiIstimaraService;


    @PostMapping(
            value = "/extract-istimara",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<IstimaraResponse> extractIstimara(
            @RequestParam("file")
            MultipartFile file
    ) {

        try {

            IstimaraData data =
                    openAiIstimaraService.extractIstimara(
                            file
                    );


            List<String> missingFields =
                    new ArrayList<>();


            if (isBlank(data.getPlate_number())) {
                missingFields.add("plate_number");
            }

            if (isBlank(data.getVehicle_make())) {
                missingFields.add("vehicle_make");
            }

            if (isBlank(data.getVehicle_model())) {
                missingFields.add("vehicle_model");
            }

            if (isBlank(data.getModel_year())) {
                missingFields.add("model_year");
            }

            if (isBlank(data.getVin())) {
                missingFields.add("vin");
            }


            List<String> issues =
                    new ArrayList<>();


            if (!missingFields.isEmpty()) {
                issues.add(
                        "FIELDS_NOT_DETECTED"
                );
            }


            /*
             * We don't have a real numerical confidence
             * from the model response.
             *
             * So don't fake one.
             */
            double score =
                    missingFields.isEmpty()
                            ? 1.0
                            : 0.0;


            boolean accepted =
                    !isBlank(data.getPlate_number())
                            ||
                            !isBlank(data.getVin());


            Quality quality =
                    new Quality(
                            accepted,
                            score,
                            issues,
                            missingFields
                    );


            IstimaraResponse response =
                    new IstimaraResponse(
                            accepted,
                            data,
                            quality
                    );


            return ResponseEntity.ok(
                    response
            );

        }

        catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new IstimaraResponse(
                                    false,
                                    null,
                                    new Quality(
                                            false,
                                            0,
                                            List.of(
                                                    e.getMessage()
                                            ),
                                            List.of()
                                    )
                            )
                    );
        }

        catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            new IstimaraResponse(
                                    false,
                                    null,
                                    new Quality(
                                            false,
                                            0,
                                            List.of(
                                                    "AI_EXTRACTION_FAILED"
                                            ),
                                            List.of()
                                    )
                            )
                    );
        }
    }


    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }
}