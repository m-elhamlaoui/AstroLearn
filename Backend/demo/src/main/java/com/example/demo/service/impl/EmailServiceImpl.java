package com.example.demo.service.impl;

import com.example.demo.model.User;
import com.example.demo.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async; // Import Async
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}") // Get sender from properties
    private String mailFrom;

    @Value("${app.admin.notification-emails}")
    private String adminEmailsCsv; // Get admin emails CSV from properties

    @Async // Send emails asynchronously to avoid blocking the main thread
    @Override
    public void sendVerificationRequestNotificationToAdmins(User requestingUser) {
        if (adminEmailsCsv == null || adminEmailsCsv.isBlank()) {
            log.warn("No admin emails configured (app.admin.notification-emails). Cannot send verification request notification.");
            return;
        }
        List<String> adminEmails = Arrays.stream(adminEmailsCsv.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toList();

        if(adminEmails.isEmpty()){
            log.warn("Admin emails property (app.admin.notification-emails) configured but contains no valid addresses.");
            return;
        }

        log.info("Sending verification request notification to admins for user: {}", requestingUser.getUsername());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(adminEmails.toArray(new String[0])); // Convert list to array
            message.setSubject("New User Verification Request: " + requestingUser.getUsername());
            message.setText(String.format(
                    "A new verification request has been submitted by user '%s' (ID: %d, Email: %s).\n\nPlease review the request in the admin panel.",
                    requestingUser.getUsername(),
                    requestingUser.getId(),
                    requestingUser.getEmail()
            ));
            mailSender.send(message);
            log.info("Verification request notification sent successfully to admins for user: {}", requestingUser.getUsername());
        } catch (MailException e) {
            log.error("Failed to send verification request email to admins for user {}: {}", requestingUser.getUsername(), e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendVerificationApprovedNotification(User targetUser) {
        log.info("Sending verification approved notification to user: {}", targetUser.getUsername());
        try {
            // Example using MimeMessage for potentially nicer HTML formatting
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(mailFrom);
            helper.setTo(targetUser.getEmail());
            helper.setSubject("Your Account Verification Approved!");

            // Basic HTML example
            String htmlMsg = String.format("""
                    <h3>Congratulations, %s!</h3>
                    <p>Your account verification request has been approved.</p>
                    <p>You can now access features available to verified users, such as creating missions.</p>
                    <p>Thank you!</p>
                    """, targetUser.getUsername());
            helper.setText(htmlMsg, true); // true = isHtml

            mailSender.send(mimeMessage);
            log.info("Verification approved notification sent successfully to user: {}", targetUser.getUsername());

        } catch (MailException | MessagingException e) {
            log.error("Failed to send verification approved email to user {}: {}", targetUser.getUsername(), e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendVerificationRejectedNotification(User targetUser, String reason) {
        log.info("Sending verification rejected notification to user: {}", targetUser.getUsername());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(targetUser.getEmail());
            message.setSubject("Account Verification Update");

            String text = String.format(
                    "Hello %s,\n\nYour account verification request could not be approved at this time.",
                    targetUser.getUsername()
            );
            if (reason != null && !reason.isBlank()) {
                text += "\n\nReason: " + reason;
            }
            text += "\n\nPlease review your profile information or contact support if you have questions.";

            message.setText(text);
            mailSender.send(message);
            log.info("Verification rejected notification sent successfully to user: {}", targetUser.getUsername());
        } catch (MailException e) {
            log.error("Failed to send verification rejected email to user {}: {}", targetUser.getUsername(), e.getMessage(), e);
        }
    }
}