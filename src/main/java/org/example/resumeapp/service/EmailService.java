package org.example.resumeapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String name, String token) {
        String link = baseUrl + "/api/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify your email - Resume Builder");
        message.setText("Hi " + name + ",\n\n"
                + "Click the link below to verify your email (valid for 24 hours):\n"
                + link + "\n\n"
                + "If you didn't create an account, you can ignore this email.");

        try {
            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }
}