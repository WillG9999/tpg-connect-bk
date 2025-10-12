package com.tpg.connect.client;

import com.tpg.connect.client.config.EmailClientConfig;
import com.tpg.connect.client.models.EmailMessage;
import com.tpg.connect.client.models.EmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Email Client
 * 
 * Handles sending emails through various providers (SMTP, SendGrid, AWS SES)
 * with support for HTML content, attachments, and batch sending.
 */
@Component
public class EmailClient {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailClient.class);
    
    private final EmailClientConfig config;
    private Session mailSession;
    
    @Autowired
    public EmailClient(EmailClientConfig config) {
        this.config = config;
        initializeMailSession();
        
        logger.info("📧 Email Client initialized with provider: {}", config.getProvider());
    }
    
    /**
     * Send a single email message
     * 
     * @param message The email message to send
     * @return EmailResponse containing the result
     */
    public CompletableFuture<EmailResponse> sendEmail(EmailMessage message) {
        logger.debug("📧 Sending email to: {}", message.getToEmail());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                validateMessage(message);
                
                MimeMessage mimeMessage = createMimeMessage(message);
                Transport.send(mimeMessage);
                
                EmailResponse response = EmailResponse.success(message.getToEmail(), generateMessageId());
                logEmailResult(message.getToEmail(), response);
                
                return response;
                
            } catch (Exception e) {
                logger.error("📧 Failed to send email to: {}", message.getToEmail(), e);
                return EmailResponse.error(message.getToEmail(), "SEND_FAILED", e.getMessage());
            }
        });
    }
    
    /**
     * Send email to multiple recipients
     * 
     * @param messages List of email messages to send
     * @return List of EmailResponse results
     */
    public CompletableFuture<List<EmailResponse>> sendBatchEmails(List<EmailMessage> messages) {
        logger.info("📧 Sending email batch to {} recipients", messages.size());
        
        List<CompletableFuture<EmailResponse>> futures = messages.stream()
            .map(this::sendEmail)
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
    }
    
    /**
     * Send email to multiple recipients with same content
     * 
     * @param toEmails List of recipient email addresses
     * @param message The email message template
     * @return List of EmailResponse results
     */
    public CompletableFuture<List<EmailResponse>> sendBulkEmail(List<String> toEmails, EmailMessage message) {
        logger.info("📧 Sending bulk email to {} recipients", toEmails.size());
        
        List<EmailMessage> messages = toEmails.stream()
            .map(email -> message.toBuilder().withToEmail(email).build())
            .toList();
        
        return sendBatchEmails(messages);
    }
    
    /**
     * Validate email address format
     * 
     * @param email The email address to validate
     * @return true if email format is valid
     */
    public boolean validateEmailAddress(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
    
    /**
     * Test email configuration by sending a test message
     * 
     * @param testEmail Email address to send test to
     * @return EmailResponse indicating success or failure
     */
    public CompletableFuture<EmailResponse> testConnection(String testEmail) {
        logger.info("📧 Testing email connection to: {}", testEmail);
        
        EmailMessage testMessage = EmailMessage.builder()
            .withFromEmail(config.getFromEmail())
            .withFromName(config.getFromName())
            .withToEmail(testEmail)
            .withSubject("Email Connection Test")
            .withTextContent("This is a test email to verify email configuration.")
            .withHtmlContent("<p>This is a test email to verify email configuration.</p>")
            .build();
        
        return sendEmail(testMessage);
    }
    
    /**
     * Initialize mail session based on configuration
     */
    private void initializeMailSession() {
        Properties props = new Properties();
        
        switch (config.getProvider().toLowerCase()) {
            case "smtp":
                setupSmtpProperties(props);
                break;
            case "sendgrid":
                setupSendGridProperties(props);
                break;
            case "ses":
                setupSesProperties(props);
                break;
            default:
                setupSmtpProperties(props); // Default to SMTP
        }
        
        if (config.isAuthenticationEnabled()) {
            mailSession = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            });
        } else {
            mailSession = Session.getInstance(props);
        }
        
        if (config.isDebugEnabled()) {
            mailSession.setDebug(true);
        }
    }
    
    /**
     * Setup SMTP properties
     */
    private void setupSmtpProperties(Properties props) {
        props.put("mail.smtp.host", config.getSmtpHost());
        props.put("mail.smtp.port", config.getSmtpPort());
        props.put("mail.smtp.auth", config.isAuthenticationEnabled());
        props.put("mail.smtp.starttls.enable", config.isStartTlsEnabled());
        props.put("mail.smtp.ssl.enable", config.isSslEnabled());
        props.put("mail.smtp.connectiontimeout", config.getConnectionTimeout());
        props.put("mail.smtp.timeout", config.getReadTimeout());
    }
    
    /**
     * Setup SendGrid properties
     */
    private void setupSendGridProperties(Properties props) {
        props.put("mail.smtp.host", "smtp.sendgrid.net");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", config.getConnectionTimeout());
        props.put("mail.smtp.timeout", config.getReadTimeout());
    }
    
    /**
     * Setup AWS SES properties
     */
    private void setupSesProperties(Properties props) {
        props.put("mail.smtp.host", "email-smtp." + config.getAwsRegion() + ".amazonaws.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", config.getConnectionTimeout());
        props.put("mail.smtp.timeout", config.getReadTimeout());
    }
    
    /**
     * Create MIME message from EmailMessage
     */
    private MimeMessage createMimeMessage(EmailMessage message) throws MessagingException, IOException {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        
        // Set sender
        InternetAddress fromAddress = new InternetAddress(message.getFromEmail(), message.getFromName());
        mimeMessage.setFrom(fromAddress);
        
        // Set recipients
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(message.getToEmail()));
        
        if (message.getCcEmails() != null && !message.getCcEmails().isEmpty()) {
            mimeMessage.setRecipients(Message.RecipientType.CC, 
                InternetAddress.parse(String.join(",", message.getCcEmails())));
        }
        
        if (message.getBccEmails() != null && !message.getBccEmails().isEmpty()) {
            mimeMessage.setRecipients(Message.RecipientType.BCC, 
                InternetAddress.parse(String.join(",", message.getBccEmails())));
        }
        
        // Set reply-to if specified
        if (message.getReplyToEmail() != null) {
            mimeMessage.setReplyTo(InternetAddress.parse(message.getReplyToEmail()));
        }
        
        // Set subject
        mimeMessage.setSubject(message.getSubject());
        
        // Set date
        mimeMessage.setSentDate(new Date());
        
        // Set content
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            // Multipart message with attachments
            MimeMultipart multipart = new MimeMultipart();
            
            // Text/HTML content
            MimeBodyPart contentPart = new MimeBodyPart();
            if (message.getHtmlContent() != null) {
                contentPart.setContent(message.getHtmlContent(), "text/html; charset=utf-8");
            } else {
                contentPart.setText(message.getTextContent());
            }
            multipart.addBodyPart(contentPart);
            
            // Attachments
            for (var attachment : message.getAttachments()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachment.getFilePath());
                attachmentPart.setFileName(attachment.getFileName());
                multipart.addBodyPart(attachmentPart);
            }
            
            mimeMessage.setContent(multipart);
        } else {
            // Simple message
            if (message.getHtmlContent() != null) {
                mimeMessage.setContent(message.getHtmlContent(), "text/html; charset=utf-8");
            } else {
                mimeMessage.setText(message.getTextContent());
            }
        }
        
        return mimeMessage;
    }
    
    /**
     * Validate email message
     */
    private void validateMessage(EmailMessage message) {
        if (message.getFromEmail() == null || message.getFromEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("From email is required");
        }
        if (message.getToEmail() == null || message.getToEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("To email is required");
        }
        if (message.getSubject() == null || message.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (message.getTextContent() == null && message.getHtmlContent() == null) {
            throw new IllegalArgumentException("Email content is required");
        }
        
        if (!validateEmailAddress(message.getFromEmail())) {
            throw new IllegalArgumentException("Invalid from email address: " + message.getFromEmail());
        }
        if (!validateEmailAddress(message.getToEmail())) {
            throw new IllegalArgumentException("Invalid to email address: " + message.getToEmail());
        }
    }
    
    /**
     * Generate message ID for tracking
     */
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + Math.random();
    }
    
    /**
     * Log email result
     */
    private void logEmailResult(String toEmail, EmailResponse response) {
        if (response.isSuccess()) {
            logger.info("📧✅ Email sent successfully to: {}, Message ID: {}", 
                toEmail, response.getMessageId());
        } else {
            logger.warn("📧❌ Email failed to: {}, Error: {}, Reason: {}", 
                toEmail, response.getErrorCode(), response.getErrorReason());
        }
    }
    
    /**
     * Get client configuration
     */
    public EmailClientConfig getConfig() {
        return config;
    }
    
    /**
     * Check if client is properly configured
     */
    public boolean isConfigured() {
        return config.isValid();
    }
}