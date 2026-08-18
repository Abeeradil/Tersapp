package org.example.tears.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() throws IOException {

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        String firebaseCredentialsPath =
                System.getenv("FIREBASE_CREDENTIALS_PATH");

        if (firebaseCredentialsPath == null ||
                firebaseCredentialsPath.isBlank()) {

            throw new IllegalStateException(
                    "FIREBASE_CREDENTIALS_PATH is not configured"
            );
        }

        try (FileInputStream serviceAccount =
                     new FileInputStream(firebaseCredentialsPath)) {

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