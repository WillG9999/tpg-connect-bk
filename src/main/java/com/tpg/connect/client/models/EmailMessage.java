package com.tpg.connect.client.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model representing an email message
 * 
 * Supports HTML/text content, attachments, CC/BCC recipients,
 * and custom headers with a builder pattern for easy construction.
 */
public class EmailMessage {
    
    /**
     * Sender email address
     */
    private String fromEmail;
    
    /**
     * Sender display name
     */
    private String fromName;
    
    /**
     * Primary recipient email address
     */
    private String toEmail;
    
    /**
     * Primary recipient display name
     */
    private String toName;
    
    /**
     * CC recipient email addresses
     */
    private List<String> ccEmails;
    
    /**
     * BCC recipient email addresses
     */
    private List<String> bccEmails;
    
    /**
     * Reply-to email address
     */
    private String replyToEmail;
    
    /**
     * Email subject
     */
    private String subject;
    
    /**
     * Plain text content
     */
    private String textContent;
    
    /**
     * HTML content
     */
    private String htmlContent;
    
    /**
     * Email attachments
     */
    private List<EmailAttachment> attachments;
    
    /**
     * Custom email headers
     */
    private Map<String, String> headers;
    
    /**
     * Email priority (1-5, where 1 is highest)
     */
    private Integer priority;
    
    /**
     * Email category/tag for tracking
     */
    private String category;
    
    /**
     * Template ID (if using templates)
     */
    private String templateId;
    
    /**
     * Template variables
     */
    private Map<String, Object> templateVariables;
    
    /**
     * Scheduled send time
     */
    private LocalDateTime scheduledAt;
    
    /**
     * Tracking enabled
     */
    private boolean trackingEnabled = false;
    
    /**
     * Click tracking enabled
     */
    private boolean clickTrackingEnabled = false;
    
    /**
     * Open tracking enabled
     */
    private boolean openTrackingEnabled = false;
    
    // Constructors
    
    public EmailMessage() {
        this.ccEmails = new ArrayList<>();
        this.bccEmails = new ArrayList<>();
        this.attachments = new ArrayList<>();
        this.headers = new HashMap<>();
        this.templateVariables = new HashMap<>();
    }
    
    // Builder pattern
    
    public static EmailMessageBuilder builder() {
        return new EmailMessageBuilder();
    }
    
    public EmailMessageBuilder toBuilder() {
        return new EmailMessageBuilder()
            .withFromEmail(fromEmail)
            .withFromName(fromName)
            .withToEmail(toEmail)
            .withToName(toName)
            .withCcEmails(ccEmails)
            .withBccEmails(bccEmails)
            .withReplyToEmail(replyToEmail)
            .withSubject(subject)
            .withTextContent(textContent)
            .withHtmlContent(htmlContent)
            .withAttachments(attachments)
            .withHeaders(headers)
            .withPriority(priority)
            .withCategory(category)
            .withTemplateId(templateId)
            .withTemplateVariables(templateVariables)
            .withScheduledAt(scheduledAt)
            .withTracking(trackingEnabled, clickTrackingEnabled, openTrackingEnabled);
    }
    
    // Getters and Setters
    
    public String getFromEmail() {
        return fromEmail;
    }
    
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
    
    public String getFromName() {
        return fromName;
    }
    
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
    
    public String getToEmail() {
        return toEmail;
    }
    
    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }
    
    public String getToName() {
        return toName;
    }
    
    public void setToName(String toName) {
        this.toName = toName;
    }
    
    public List<String> getCcEmails() {
        return ccEmails;
    }
    
    public void setCcEmails(List<String> ccEmails) {
        this.ccEmails = ccEmails != null ? ccEmails : new ArrayList<>();
    }
    
    public List<String> getBccEmails() {
        return bccEmails;
    }
    
    public void setBccEmails(List<String> bccEmails) {
        this.bccEmails = bccEmails != null ? bccEmails : new ArrayList<>();
    }
    
    public String getReplyToEmail() {
        return replyToEmail;
    }
    
    public void setReplyToEmail(String replyToEmail) {
        this.replyToEmail = replyToEmail;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getTextContent() {
        return textContent;
    }
    
    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
    
    public String getHtmlContent() {
        return htmlContent;
    }
    
    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }
    
    public List<EmailAttachment> getAttachments() {
        return attachments;
    }
    
    public void setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers != null ? headers : new HashMap<>();
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
    
    public Map<String, Object> getTemplateVariables() {
        return templateVariables;
    }
    
    public void setTemplateVariables(Map<String, Object> templateVariables) {
        this.templateVariables = templateVariables != null ? templateVariables : new HashMap<>();
    }
    
    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }
    
    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
    
    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }
    
    public void setTrackingEnabled(boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }
    
    public boolean isClickTrackingEnabled() {
        return clickTrackingEnabled;
    }
    
    public void setClickTrackingEnabled(boolean clickTrackingEnabled) {
        this.clickTrackingEnabled = clickTrackingEnabled;
    }
    
    public boolean isOpenTrackingEnabled() {
        return openTrackingEnabled;
    }
    
    public void setOpenTrackingEnabled(boolean openTrackingEnabled) {
        this.openTrackingEnabled = openTrackingEnabled;
    }
    
    // Utility methods
    
    /**
     * Check if message has HTML content
     */
    public boolean hasHtmlContent() {
        return htmlContent != null && !htmlContent.trim().isEmpty();
    }
    
    /**
     * Check if message has attachments
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }
    
    /**
     * Check if message is scheduled
     */
    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }
    
    /**
     * Check if message uses template
     */
    public boolean usesTemplate() {
        return templateId != null && !templateId.trim().isEmpty();
    }
    
    /**
     * Get total recipient count
     */
    public int getRecipientCount() {
        int count = 1; // Primary recipient
        if (ccEmails != null) count += ccEmails.size();
        if (bccEmails != null) count += bccEmails.size();
        return count;
    }
    
    /**
     * Add CC recipient
     */
    public void addCcEmail(String email) {
        if (ccEmails == null) ccEmails = new ArrayList<>();
        ccEmails.add(email);
    }
    
    /**
     * Add BCC recipient
     */
    public void addBccEmail(String email) {
        if (bccEmails == null) bccEmails = new ArrayList<>();
        bccEmails.add(email);
    }
    
    /**
     * Add attachment
     */
    public void addAttachment(EmailAttachment attachment) {
        if (attachments == null) attachments = new ArrayList<>();
        attachments.add(attachment);
    }
    
    /**
     * Add custom header
     */
    public void addHeader(String name, String value) {
        if (headers == null) headers = new HashMap<>();
        headers.put(name, value);
    }
    
    /**
     * Add template variable
     */
    public void addTemplateVariable(String name, Object value) {
        if (templateVariables == null) templateVariables = new HashMap<>();
        templateVariables.put(name, value);
    }
    
    @Override
    public String toString() {
        return "EmailMessage{" +
                "fromEmail='" + fromEmail + '\'' +
                ", fromName='" + fromName + '\'' +
                ", toEmail='" + toEmail + '\'' +
                ", toName='" + toName + '\'' +
                ", ccEmails=" + (ccEmails != null ? ccEmails.size() : 0) +
                ", bccEmails=" + (bccEmails != null ? bccEmails.size() : 0) +
                ", replyToEmail='" + replyToEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", hasTextContent=" + (textContent != null) +
                ", hasHtmlContent=" + (htmlContent != null) +
                ", attachments=" + (attachments != null ? attachments.size() : 0) +
                ", headers=" + (headers != null ? headers.size() : 0) +
                ", priority=" + priority +
                ", category='" + category + '\'' +
                ", templateId='" + templateId + '\'' +
                ", templateVariables=" + (templateVariables != null ? templateVariables.size() : 0) +
                ", scheduledAt=" + scheduledAt +
                ", trackingEnabled=" + trackingEnabled +
                ", clickTrackingEnabled=" + clickTrackingEnabled +
                ", openTrackingEnabled=" + openTrackingEnabled +
                '}';
    }
    
    /**
     * Builder class for EmailMessage
     */
    public static class EmailMessageBuilder {
        private final EmailMessage message;
        
        public EmailMessageBuilder() {
            this.message = new EmailMessage();
        }
        
        public EmailMessageBuilder withFromEmail(String fromEmail) {
            message.setFromEmail(fromEmail);
            return this;
        }
        
        public EmailMessageBuilder withFromName(String fromName) {
            message.setFromName(fromName);
            return this;
        }
        
        public EmailMessageBuilder withToEmail(String toEmail) {
            message.setToEmail(toEmail);
            return this;
        }
        
        public EmailMessageBuilder withToName(String toName) {
            message.setToName(toName);
            return this;
        }
        
        public EmailMessageBuilder withCcEmails(List<String> ccEmails) {
            message.setCcEmails(ccEmails);
            return this;
        }
        
        public EmailMessageBuilder withBccEmails(List<String> bccEmails) {
            message.setBccEmails(bccEmails);
            return this;
        }
        
        public EmailMessageBuilder withReplyToEmail(String replyToEmail) {
            message.setReplyToEmail(replyToEmail);
            return this;
        }
        
        public EmailMessageBuilder withSubject(String subject) {
            message.setSubject(subject);
            return this;
        }
        
        public EmailMessageBuilder withTextContent(String textContent) {
            message.setTextContent(textContent);
            return this;
        }
        
        public EmailMessageBuilder withHtmlContent(String htmlContent) {
            message.setHtmlContent(htmlContent);
            return this;
        }
        
        public EmailMessageBuilder withAttachments(List<EmailAttachment> attachments) {
            message.setAttachments(attachments);
            return this;
        }
        
        public EmailMessageBuilder withAttachment(EmailAttachment attachment) {
            message.addAttachment(attachment);
            return this;
        }
        
        public EmailMessageBuilder withHeaders(Map<String, String> headers) {
            message.setHeaders(headers);
            return this;
        }
        
        public EmailMessageBuilder withHeader(String name, String value) {
            message.addHeader(name, value);
            return this;
        }
        
        public EmailMessageBuilder withPriority(Integer priority) {
            message.setPriority(priority);
            return this;
        }
        
        public EmailMessageBuilder withCategory(String category) {
            message.setCategory(category);
            return this;
        }
        
        public EmailMessageBuilder withTemplateId(String templateId) {
            message.setTemplateId(templateId);
            return this;
        }
        
        public EmailMessageBuilder withTemplateVariables(Map<String, Object> templateVariables) {
            message.setTemplateVariables(templateVariables);
            return this;
        }
        
        public EmailMessageBuilder withTemplateVariable(String name, Object value) {
            message.addTemplateVariable(name, value);
            return this;
        }
        
        public EmailMessageBuilder withScheduledAt(LocalDateTime scheduledAt) {
            message.setScheduledAt(scheduledAt);
            return this;
        }
        
        public EmailMessageBuilder withTracking(boolean tracking, boolean clickTracking, boolean openTracking) {
            message.setTrackingEnabled(tracking);
            message.setClickTrackingEnabled(clickTracking);
            message.setOpenTrackingEnabled(openTracking);
            return this;
        }
        
        public EmailMessage build() {
            return message;
        }
    }
}