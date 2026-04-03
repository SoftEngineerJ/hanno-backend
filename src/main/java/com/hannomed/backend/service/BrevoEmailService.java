package com.hannomed.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.api.TransactionalEmailsApi;
import sendinblue.model.SendSmtpEmail;
import sendinblue.model.SendSmtpEmailSender;
import sendinblue.model.SendSmtpEmailTo;

import java.util.Arrays;
import java.util.Collections;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrevoEmailService {

    @Value("${BREVO_API_KEY:}")
    private String brevoApiKey;

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
            log.info("Brevo API - Attempting to send email to: {}", email);

            ApiClient defaultClient = Configuration.getDefaultApiClient();
            defaultClient.setApiKey(brevoApiKey);

            TransactionalEmailsApi api = new TransactionalEmailsApi();
            
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail("mazroo.develop@gmail.com");
            sender.setName("HannoApp");

            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(email);
            to.setName(firstName + " " + lastName);

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

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Collections.singletonList(to));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            api.sendTransacEmail(sendSmtpEmail);
            log.info("Brevo API - Email sent successfully to: {}", email);

        } catch (ApiException e) {
            log.error("Brevo API - ApiException: {} | Response: {}", e.getMessage(), e.getResponseBody());
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Brevo API - Exception: {} | Cause: {}", e.getMessage(), e.getCause());
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        }
    }

    public void sendPasswordChangedEmail(String email, String firstName) {
        try {
            log.info("Brevo API - Sending password change email to: {}", email);

            ApiClient defaultClient = Configuration.getDefaultApiClient();
            defaultClient.setApiKey(brevoApiKey);

            TransactionalEmailsApi api = new TransactionalEmailsApi();
            
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail("mazroo.develop@gmail.com");
            sender.setName("HannoApp");

            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(email);
            to.setName(firstName);

            String subject = "Dein Passwort wurde geändert - HannoApp";
            String htmlContent = "<html><body>" +
                    "<p>Hallo " + firstName + ",</p>" +
                    "<p>Dein Passwort wurde erfolgreich geändert.</p>" +
                    "<p>Wenn Du diese Änderung nicht selbst vorgenommen hast, wende Dich bitte umgehend an Deinen Administrator.</p>" +
                    "<p>Viele Grüße,<br>Dein HannoApp Team</p>" +
                    "</body></html>";

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Collections.singletonList(to));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            api.sendTransacEmail(sendSmtpEmail);
            log.info("Brevo API - Password change email sent successfully to: {}", email);

        } catch (ApiException e) {
            log.error("Brevo API - ApiException: {} | Response: {}", e.getMessage(), e.getResponseBody());
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Brevo API - Exception: {} | Cause: {}", e.getMessage(), e.getCause());
            throw new RuntimeException("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        }
    }
}
