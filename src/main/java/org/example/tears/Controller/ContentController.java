package org.example.tears.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tears/content")
@RequiredArgsConstructor

public class ContentController {


        @GetMapping("/terms")
        public Map<String, Object> getTerms() throws Exception {

            ObjectMapper mapper = new ObjectMapper();

            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("content/terms.json");

            if (is == null) {
                throw new RuntimeException("terms.json not found");
            }

            return mapper.readValue(is, Map.class);
        }

        @GetMapping("/privacy")
        public Map<String, Object> getPrivacyPolicy() throws Exception {

            ObjectMapper mapper = new ObjectMapper();

            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("content/privacy.json");

            if (is == null) {
                throw new RuntimeException("privacy.json not found");
            }

            return mapper.readValue(is, Map.class);
        }

    @GetMapping("/faqs")
    public Map<String, Object> getFaqs() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("content/faqs.json");

        if (is == null) {
            throw new RuntimeException("faqs.json not found");
        }

        return mapper.readValue(is, Map.class);
    }
}
