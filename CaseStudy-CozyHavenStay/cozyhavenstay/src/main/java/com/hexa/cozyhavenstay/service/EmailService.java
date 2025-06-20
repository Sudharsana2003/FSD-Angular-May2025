// src/main/java/com/hexa/cozyhavenstay/service/EmailService.java
package com.hexa.cozyhavenstay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException; // Import for general mail exceptions
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper; // NEW IMPORT
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException; // NEW IMPORT
import jakarta.mail.internet.MimeMessage; // NEW IMPORT

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Optional: You might want to define a sender email in application.properties
    // @Value("${spring.mail.username}")
    // private String senderEmail;

    /**
     * Sends a simple plain-text email.
     * @param to The recipient email address.
     * @param subject The subject of the email.
     * @param body The plain text body of the email.
     */
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        // If you have a default 'from' address configured in application.properties, it will use that.
        // Otherwise, you can uncomment and set it:
        // message.setFrom(senderEmail); // e.g., "noreply@cozyhavenstay.com"

        try {
            mailSender.send(message);
            System.out.println("Plain text email sent to " + to); // For debugging
        } catch (MailException e) {
            System.err.println("Failed to send plain text email to " + to + ": " + e.getMessage());
            // Log the exception properly in a real application
            throw new RuntimeException("Error sending plain text email", e);
        }
    }

    /**
     * Sends an HTML-formatted email.
     * This is the method you'll use for password reset links.
     * @param to The recipient email address.
     * @param subject The subject of the email.
     * @param htmlBody The HTML content of the email body.
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            // The 'true' argument indicates a multipart message, needed for HTML content
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // Explicitly set UTF-8 encoding

            helper.setFrom("your_application_email@example.com"); // <-- IMPORTANT: Replace with your actual sender email
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // The 'true' here is crucial: it tells the helper the content is HTML

            mailSender.send(message);
            System.out.println("HTML email sent to " + to); // For debugging
        } catch (MessagingException | MailException e) {
            System.err.println("Failed to send HTML email to " + to + ": " + e.getMessage());
            // Log the exception properly in a real application
            throw new RuntimeException("Error sending HTML email", e);
        }
    }
}