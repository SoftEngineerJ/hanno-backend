package com.hannomed.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
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
        try {
            log.info("SMTP Debug - Attempting to send email to: {}", email);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Willkommen bei HannoApp - Deine Zugangsdaten");

            String body = "Hallo " + firstName + " " + lastName + ",\n\n" +
                    "wir freuen uns, Dir unsere neue HannoApp vorstellen zu duerfen!\n\n" +
                    "HannoApp ist unsere interne Mitarbeiter-App fuer Urlaubsantraege, " +
                    "Freizeitausgleich und mehr. Hier kannst Du bequem Antrraege stellen " +
                    "und den Status verfolgen.\n\n" +
                    "Deine Zugangsdaten:\n" +
                    "Benutzername: " + username + "\n" +
                    "Passwort: " + password + "\n\n" +
                    "Bitte aendere Dein Passwort nach dem ersten Login.\n\n" +
                    "App herunterladen:\n" +
                    "https://hannomed.vercel.app\n\n" +
                    "Bei Fragen wende Dich an Deinen Administrator.\n\n" +
                    "Viele Gruesse,\n" +
                    "Dein HannoApp Team";

            helper.setText(body, false);
            log.info("SMTP Debug - Sending message...");
            mailSender.send(message);
            log.info("SMTP Debug - Email sent successfully to: {}", email);
        } catch (MessagingException e) {
            log.error("SMTP Debug - MessagingException: {} | Cause: {}", e.getMessage(), e.getCause());
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("SMTP Debug - Exception: {} | Cause: {}", e.getMessage(), e.getCause());
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        }
    }

    public void sendPasswordChangedEmail(String email, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Dein Passwort wurde geaendert - HannoApp");

            String body = "Hallo " + firstName + ",\n\n" +
                    "Dein Passwort wurde erfolgreich geaendert.\n\n" +
                    "Wenn Du diese Aenderung nicht selbst vorgenommen hast, wende Dich bitte umgehend an Deinen Administrator.\n\n"
                    +
                    "Viele Gruesse,\n" +
                    "Dein HannoApp Team";

            helper.setText(body, false);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        }
    }
}
