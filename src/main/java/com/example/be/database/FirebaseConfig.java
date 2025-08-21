package com.example.be.database;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private final String FIREBASE_KEY_FILENAME = "eyes-of-breath-firebase-adminsdk-fbsvc-a2063d8b4b.json";

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        ClassPathResource resource = new ClassPathResource(FIREBASE_KEY_FILENAME);
        InputStream serviceAccount = resource.getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket("eyes-of-breath.appspot.com")
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("FirebaseApp initializing...");
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public Storage storage() throws IOException {
        ClassPathResource resource = new ClassPathResource(FIREBASE_KEY_FILENAME);
        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }
}