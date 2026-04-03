package com.hannomed.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";
    private static final int PASSWORD_LENGTH = 12;

    public String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return password.toString();
    }

    public void sendWelcomeEmail(String email, String firstName, String lastName, String username, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom("HannoMed App");
        message.setSubject("Willkommen bei HannoApp - Ihre Zugangsdaten");

        String body = """
                Hallo %s %s,

                herzlich willkommen bei HannoApp!

                Ihre Zugangsdaten für die App:

                Benutzername: %s
                Passwort: %s

                Bitte ändern Sie Ihr Passwort nach dem ersten Login.

                Die App können Sie hier herunterladen:
                - Android: Google Play Store (Suche nach "HannoApp")
                - iOS: Apple App Store (Suche nach "HannoApp")

                Bei Fragen wenden Sie sich an Ihren Administrator.

                Viele Grüße,
                Ihr HannoApp Team
                """.formatted(firstName, lastName, username, password);

        message.setText(body);
        mailSender.send(message);
    }

    public void sendPasswordChangedEmail(String email, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom("HannoMed App");
        message.setSubject("Ihr Passwort wurde geändert - HannoApp");

        String body = """
                Hallo %s,

                Ihr Passwort wurde erfolgreich geändert.

                Wenn Sie diese Änderung nicht selbst vorgenommen haben, wenden Sie sich bitte umgehend an Ihren Administrator.

                Viele Grüße,
                Ihr HannoApp Team
                """
                .formatted(firstName);

        message.setText(body);
        mailSender.send(message);
    }
}
