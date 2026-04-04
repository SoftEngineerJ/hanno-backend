package com.hannomed.backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class PushNotificationService {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty() && credentialsPath != null && !credentialsPath.isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(credentialsPath).getInputStream()))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
        }
    }

    public void sendPushNotification(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("No FCM token provided, skipping push notification");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().sendAsync(message).get();
            log.info("Push notification sent successfully: {}", response);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send push notification: {}", e.getMessage());
        }
    }

    public void sendRequestStatusNotification(String fcmToken, String employeeName, String status, String type) {
        String title;
        String body;

        switch (status.toLowerCase()) {
            case "approved":
            case "genehmigt":
                title = "Antrag genehmigt ✅";
                body = String.format("Dein %s-Antrag wurde genehmigt.", type);
                break;
            case "rejected":
            case "abgelehnt":
                title = "Antrag abgelehnt ❌";
                body = String.format("Dein %s-Antrag wurde leider abgelehnt.", type);
                break;
            default:
                title = "Antragsstatus aktualisiert";
                body = String.format("Dein %s-Antrag wurde aktualisiert: %s", type, status);
        }

        sendPushNotification(fcmToken, title, body);
    }
}
