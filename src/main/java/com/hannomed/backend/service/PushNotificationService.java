package com.hannomed.backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class PushNotificationService {

    @Value("${firebase.project.id:}")
    private String projectId;

    @Value("${firebase.private.key.id:}")
    private String privateKeyId;

    @Value("${firebase.private.key:}")
    private String privateKey;

    @Value("${firebase.client.email:}")
    private String clientEmail;

    @Value("${firebase.client.id:}")
    private String clientId;

    @Value("${firebase.auth.uri:https://accounts.google.com/o/oauth2/auth}")
    private String authUri;

    @Value("${firebase.token.uri:https://oauth2.googleapis.com/token}")
    private String tokenUri;

    @Value("${firebase.auth.provider.x509.cert.url:https://www.googleapis.com/oauth2/v1/certs}")
    private String authProviderCertUrl;

    @Value("${firebase.client.x509.cert.url:}")
    private String clientCertUrl;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                String credentialsPath = System.getenv().getOrDefault("FIREBASE_CREDENTIALS_FILE",
                        "/etc/secrets/firebase-credentials.json");

                String json;
                if (Files.exists(Paths.get(credentialsPath))) {
                    json = Files.readString(Paths.get(credentialsPath));
                    log.info("Firebase credentials loaded from file: {}", credentialsPath);
                } else {
                    log.warn("Firebase credentials file not found at: {}, falling back to env vars", credentialsPath);
                    if (projectId == null || projectId.isEmpty() || privateKey == null
                            || privateKey.isEmpty() || clientEmail == null || clientEmail.isEmpty()) {
                        log.error("Firebase credentials not configured");
                        return;
                    }
                    json = String.format(
                            "{\"type\":\"service_account\",\"project_id\":\"%s\",\"private_key_id\":\"%s\",\"private_key\":\"%s\",\"client_email\":\"%s\",\"client_id\":\"%s\",\"auth_uri\":\"%s\",\"token_uri\":\"%s\",\"auth_provider_x509_cert_url\":\"%s\",\"client_x509_cert_url\":\"%s\",\"universe_domain\":\"googleapis.com\"}",
                            projectId,
                            privateKeyId,
                            privateKey.replace("\n", "\\n"),
                            clientEmail,
                            clientId,
                            authUri,
                            tokenUri,
                            authProviderCertUrl,
                            clientCertUrl);
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(
                                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))))
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
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
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
