package org.example.tears.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() throws IOException {

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        String firebaseCredentials =
                System.getenv("FIREBASE_CREDENTIALS_BASE64");

        if (firebaseCredentials == null ||
                firebaseCredentials.isBlank()) {

            throw new IllegalStateException(
                    "FIREBASE_CREDENTIALS_BASE64 is not configured"
            );
        }

        byte[] decodedCredentials =
                Base64.getDecoder().decode(firebaseCredentials);

        try (InputStream serviceAccount =
                     new ByteArrayInputStream(decodedCredentials)) {

            FirebaseOptions options =
                    FirebaseOptions.builder()
                            .setCredentials(
                                    GoogleCredentials.fromStream(
                                            serviceAccount
                                    )
                            )
                            .build();

            FirebaseApp.initializeApp(options);
        }
    }
}