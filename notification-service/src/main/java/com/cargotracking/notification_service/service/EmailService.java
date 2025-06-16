package com.cargotracking.notification_service.service;

import com.cargotracking.notification_service.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * EmailService - Email bildirimleri gönderme servisi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.from}")
    private String fromEmail;
    
    @Value("${email.provider.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${email.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${email.retry.delay:5000}")
    private long retryDelay;
    
    /**
     * Basit email gönderimi - retry logic ile
     */
    public boolean sendEmail(Notification notification) {
        if (!emailEnabled) {
            log.warn("📧 Email service is disabled");
            return false;
        }
        
        // Recipient validation
        if (notification.getRecipient() == null || notification.getRecipient().trim().isEmpty()) {
            log.warn("📧 Cannot send email: recipient is null or empty");
            return false;
        }
        
        // Email format validation
        if (!isValidEmail(notification.getRecipient())) {
            log.warn("📧 Invalid email format: {}", notification.getRecipient());
            return false;
        }
        
        return sendEmailWithRetry(notification, maxRetryAttempts);
    }
    
    /**
     * Retry logic ile email gönderimi
     */
    private boolean sendEmailWithRetry(Notification notification, int attemptsLeft) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getTitle());
            message.setText(notification.getMessage());
            message.setSentDate(new java.util.Date());
            
            mailSender.send(message);
            log.info("📧 Email sent successfully to: {}", notification.getRecipient());
            return true;
            
        } catch (Exception e) {
            log.error("📧 Failed to send email to: {} (attempts left: {})", 
                     notification.getRecipient(), attemptsLeft - 1, e);
            
            if (attemptsLeft > 1) {
                try {
                    Thread.sleep(retryDelay);
                    return sendEmailWithRetry(notification, attemptsLeft - 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return false;
        }
    }
    
    /**
     * HTML email gönderimi (template ile)
     */
    public boolean sendHtmlEmail(Notification notification, String templateName) {
        // Recipient validation
        if (notification.getRecipient() == null || notification.getRecipient().trim().isEmpty()) {
            log.warn("Cannot send HTML email: recipient is null or empty");
            return false;
        }
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getTitle());
            helper.setSentDate(new java.util.Date());
            
            // Thymeleaf template'i işle
            Context context = new Context();
            if (notification.getTemplateData() != null) {
                for (Map.Entry<String, Object> entry : notification.getTemplateData().entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }
            
            // Genel değişkenler
            context.setVariable("notificationTitle", notification.getTitle());
            context.setVariable("notificationMessage", notification.getMessage());
            context.setVariable("currentDate", LocalDateTime.now());
            
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", notification.getRecipient());
            return true;
            
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", notification.getRecipient(), e);
            return false;
        }
    }
    
    /**
     * Toplu email gönderimi
     */
    public void sendBulkEmail(String[] recipients, String subject, String content) {
        for (String recipient : recipients) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(recipient);
                message.setSubject(subject);
                message.setText(content);
                message.setSentDate(new java.util.Date());
                
                mailSender.send(message);
                log.debug("Bulk email sent to: {}", recipient);
                
            } catch (Exception e) {
                log.error("Failed to send bulk email to: {}", recipient, e);
            }
        }
    }
    
    /**
     * Email template test
     */
    public boolean testEmailConnection() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail);
            message.setSubject("Test Email Connection");
            message.setText("This is a test email to verify email configuration.");
            message.setSentDate(new java.util.Date());
            
            mailSender.send(message);
            log.info("Email connection test successful");
            return true;
            
        } catch (Exception e) {
            log.error("Email connection test failed", e);
            return false;
        }
    }
    
    /**
     * Email format validation
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basit email regex validation
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Bulk email gönderimi - geliştirilmiş
     */
    public int sendBulkEmailAdvanced(java.util.List<Notification> notifications) {
        int successCount = 0;
        for (Notification notification : notifications) {
            if (sendEmail(notification)) {
                successCount++;
            }
            
            // Rate limiting için kısa bekleme
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("📧 Bulk email completed: {}/{} successful", successCount, notifications.size());
        return successCount;
    }
} 