package com.hannomed.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BrevoEmailService {

    @Value("${BREVO_API_KEY:}")
    private String brevoApiKey;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";
    private static final int PASSWORD_LENGTH = 12;
    private static final String BREVO_API_URL = "https://api.sendinblue.com/v3/smtp/email";

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
            log.info("Brevo API - Attempting to send email to: {}", email);

            String subject = "Willkommen bei HannoApp - Deine Zugangsdaten";
            String htmlContent = "<html><body>" +
                    "<p>Hallo " + firstName + " " + lastName + ",</p>" +
                    "<p>wir freuen uns, Dir unsere neue HannoApp vorstellen zu können!</p>" +
                    "<p>HannoApp ist unsere interne Mitarbeiter-App für Urlaubsanträge, " +
                    "Freizeitausgleich und mehr. Hier kannst Du bequem Anträge stellen " +
                    "und den Status verfolgen.</p>" +
                    "<h3>Deine Zugangsdaten:</h3>" +
                    "<p><strong>Benutzername:</strong> " + username + "<br>" +
                    "<strong>Passwort:</strong> " + password + "</p>" +
                    "<p>Bitte ändere Dein Passwort nach dem ersten Login.</p>" +
                    "<p><strong>App herunterladen:</strong><br>" +
                    "<a href='https://hannomed.vercel.app'>https://hannomed.vercel.app</a></p>" +
                    "<p>Bei Fragen wende Dich an Deinen Administrator.</p>" +
                    "<p>Viele Grüße,<br>Dein HannoApp Team</p>" +
                    "</body></html>";

            sendEmail(email, firstName + " " + lastName, subject, htmlContent);
            log.info("Brevo API - Email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Brevo API - Exception: {} | Cause: {}", e.getMessage(), e.getCause());
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        }
    }

    public void sendPasswordChangedEmail(String email, String firstName) {
        try {
            log.info("Brevo API - Sending password change email to: {}", email);

            String subject = "Dein Passwort wurde geändert - HannoApp";
            String htmlContent = "<html><body>" +
                    "<p>Hallo " + firstName + ",</p>" +
                    "<p>Dein Passwort wurde erfolgreich geändert.</p>" +
                    "<p>Wenn Du diese Änderung nicht selbst vorgenommen hast, wende Dich bitte umgehend an Deinen Administrator.</p>"
                    +
                    "<p>Viele Grüße,<br>Dein HannoApp Team</p>" +
                    "</body></html>";

            sendEmail(email, firstName, subject, htmlContent);
            log.info("Brevo API - Password change email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Brevo API - Exception: {} | Cause: {}", e.getMessage(), e.getCause());
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        }
    }

    public void sendResetPasswordEmail(String email, String firstName, String username, String newPassword) {
        try {
            log.info("Brevo API - Sending reset password email to: {}", email);

            String subject = "Deine Zugangsdaten für HannoApp";
            String htmlContent = "<html><body>" +
                    "<p>Hallo " + firstName + ",</p>" +
                    "<p>Du hast Deine Zugangsdaten angefordert. Hier sind Deine aktuellen Daten:</p>" +
                    "<h3>Deine Zugangsdaten:</h3>" +
                    "<p><strong>Benutzername:</strong> " + username + "<br>" +
                    "<strong>Neues Passwort:</strong> " + newPassword + "</p>" +
                    "<p>Bitte melde Dich mit diesen Daten an und ändere Dein Passwort nach dem Login.</p>" +
                    "<p>Bei Fragen wende Dich an Deinen Administrator.</p>" +
                    "<p>Viele Grüße,<br>Dein HannoApp Team</p>" +
                    "</body></html>";

            sendEmail(email, firstName, subject, htmlContent);
            log.info("Brevo API - Reset password email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Brevo API - Exception: {} | Cause: {}", e.getMessage(), e.getCause());
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        }
    }

    public void sendStatusChangeEmail(String email, String firstName, String requestType, String status,
            String startDate, String endDate) {
        try {
            log.info("Brevo API - Sending status change email to: {}", email);

            String statusText;
            String statusEmoji;
            switch (status) {
                case "genehmigt":
                    statusText = "genehmigt";
                    statusEmoji = "✅";
                    break;
                case "abgelehnt":
                    statusText = "abgelehnt";
                    statusEmoji = "❌";
                    break;
                case "storniert":
                    statusText = "storniert";
                    statusEmoji = "🚫";
                    break;
                case "stornierung_abgelehnt":
                    statusText = "Stornierung abgelehnt";
                    statusEmoji = "❌";
                    break;
                default:
                    statusText = status;
                    statusEmoji = "📋";
            }

            String requestTypeText;
            switch (requestType) {
                case "vacation":
                    requestTypeText = "Urlaub";
                    break;
                case "special":
                    requestTypeText = "Sonderurlaub";
                    break;
                case "compensation":
                    requestTypeText = "Freizeitausgleich";
                    break;
                default:
                    requestTypeText = requestType;
            }

            String formattedStartDate = formatDateGerman(startDate);
            String formattedEndDate = formatDateGerman(endDate);

            String subject = "Antragsstatus aktualisiert - " + requestTypeText + " " + statusEmoji;
            String htmlContent = "<html><body>" +
                    "<p>Hallo " + firstName + ",</p>" +
                    "<p>Der Status deines Antrags wurde aktualisiert:</p>" +
                    "<div style='background-color: #f5f5f5; padding: 16px; border-radius: 8px; margin: 16px 0;'>" +
                    "<p style='margin: 0;'><strong>Antragstyp:</strong> " + requestTypeText + "</p>" +
                    "<p style='margin: 8px 0 0 0;'><strong>Zeitraum:</strong> " + formattedStartDate + " bis "
                    + formattedEndDate + "</p>" +
                    "<p style='margin: 8px 0 0 0;'><strong>Status:</strong> " + statusEmoji + " " + statusText + "</p>"
                    +
                    "</div>" +
                    "<p>Du kannst den Status auch in der App einsehen.</p>" +
                    "<p>Viele Grüße,<br>Dein HannoApp Team</p>" +
                    "</body></html>";

            sendEmail(email, firstName, subject, htmlContent);
            log.info("Brevo API - Status change email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Brevo API - Exception: {} | Cause: {}", e.getMessage(), e.getCause());
        }
    }

    private String formatDateGerman(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("-"))
            return "-";
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }

    public void sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        Map<String, Object> emailData = Map.of(
                "sender", Map.of("email", "mazroo.develop@gmail.com", "name", "HannoApp"),
                "to", List.of(Map.of("email", toEmail, "name", toName)),
                "subject", subject,
                "htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                BREVO_API_URL,
                request,
                String.class);

        if (response.getStatusCode() != HttpStatus.CREATED && response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Brevo API error: " + response.getBody());
        }
    }
}
