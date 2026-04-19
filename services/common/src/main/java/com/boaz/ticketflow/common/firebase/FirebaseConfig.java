package com.boaz.ticketflow.common.firebase;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.boaz.ticketflow.common.utils.AssetsData;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.messaging.FirebaseMessaging;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        InputStream serviceAccount = getClass().getResourceAsStream(
            AssetsData.firebaseResourcePath);
    
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setStorageBucket(AssetsData.bucketName)
            .build();

        // Vérifie si Firebase est déjà initialisé
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public Bucket firebaseStorageBucket(FirebaseApp firebaseApp) {
        return StorageClient.getInstance(firebaseApp).bucket(); // ✅ Fonctionnera maintenant
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

}





//FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase_service.json");