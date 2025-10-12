package com.tpg.connect.client.models;

import java.io.File;
import java.time.LocalDateTime;

/**
 * Model representing an email attachment
 * 
 * Supports file attachments with metadata including content type,
 * disposition, and size information for email sending.
 */
public class EmailAttachment {
    
    /**
     * File name as it appears in the email
     */
    private String fileName;
    
    /**
     * Full path to the file on disk
     */
    private String filePath;
    
    /**
     * MIME content type
     */
    private String contentType;
    
    /**
     * Content disposition: "attachment" or "inline"
     */
    private String disposition = "attachment";
    
    /**
     * Content ID for inline attachments
     */
    private String contentId;
    
    /**
     * File size in bytes
     */
    private long fileSize;
    
    /**
     * Description of the attachment
     */
    private String description;
    
    /**
     * Whether this is an inline attachment
     */
    private boolean inline = false;
    
    /**
     * Attachment creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Base64 encoded content (alternative to file path)
     */
    private String base64Content;
    
    // Constructors
    
    public EmailAttachment() {
        this.createdAt = LocalDateTime.now();
    }
    
    public EmailAttachment(String fileName, String filePath) {
        this();
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = calculateFileSize();
        this.contentType = determineContentType();
    }
    
    public EmailAttachment(String fileName, String filePath, String contentType) {
        this();
        this.fileName = fileName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = calculateFileSize();
    }
    
    // Static factory methods
    
    public static EmailAttachment fromFile(String filePath) {
        File file = new File(filePath);
        return new EmailAttachment(file.getName(), filePath);
    }
    
    public static EmailAttachment fromFile(String fileName, String filePath) {
        return new EmailAttachment(fileName, filePath);
    }
    
    public static EmailAttachment fromFile(String fileName, String filePath, String contentType) {
        return new EmailAttachment(fileName, filePath, contentType);
    }
    
    public static EmailAttachment fromBase64(String fileName, String base64Content, String contentType) {
        EmailAttachment attachment = new EmailAttachment();
        attachment.fileName = fileName;
        attachment.base64Content = base64Content;
        attachment.contentType = contentType;
        attachment.fileSize = calculateBase64Size(base64Content);
        return attachment;
    }
    
    public static EmailAttachment inline(String fileName, String filePath, String contentId) {
        EmailAttachment attachment = new EmailAttachment(fileName, filePath);
        attachment.inline = true;
        attachment.disposition = "inline";
        attachment.contentId = contentId;
        return attachment;
    }
    
    public static EmailAttachment inlineBase64(String fileName, String base64Content, String contentType, String contentId) {
        EmailAttachment attachment = fromBase64(fileName, base64Content, contentType);
        attachment.inline = true;
        attachment.disposition = "inline";
        attachment.contentId = contentId;
        return attachment;
    }
    
    // Builder methods
    
    public EmailAttachment withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
    
    public EmailAttachment withDisposition(String disposition) {
        this.disposition = disposition;
        return this;
    }
    
    public EmailAttachment withContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }
    
    public EmailAttachment withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public EmailAttachment asInline() {
        this.inline = true;
        this.disposition = "inline";
        return this;
    }
    
    public EmailAttachment asAttachment() {
        this.inline = false;
        this.disposition = "attachment";
        return this;
    }
    
    // Getters and Setters
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
        this.fileSize = calculateFileSize();
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public String getDisposition() {
        return disposition;
    }
    
    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }
    
    public String getContentId() {
        return contentId;
    }
    
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isInline() {
        return inline;
    }
    
    public void setInline(boolean inline) {
        this.inline = inline;
        this.disposition = inline ? "inline" : "attachment";
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getBase64Content() {
        return base64Content;
    }
    
    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
        this.fileSize = calculateBase64Size(base64Content);
    }
    
    // Utility methods
    
    /**
     * Check if attachment exists on disk
     */
    public boolean exists() {
        if (filePath == null) return base64Content != null;
        return new File(filePath).exists();
    }
    
    /**
     * Check if attachment is valid
     */
    public boolean isValid() {
        return fileName != null && !fileName.trim().isEmpty() &&
               (exists() || base64Content != null) &&
               contentType != null && !contentType.trim().isEmpty();
    }
    
    /**
     * Get file extension
     */
    public String getFileExtension() {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }
    
    /**
     * Check if attachment is an image
     */
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }
    
    /**
     * Check if attachment is a document
     */
    public boolean isDocument() {
        return contentType != null && (
            contentType.startsWith("application/") ||
            contentType.startsWith("text/") ||
            contentType.equals("application/pdf")
        );
    }
    
    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Calculate file size from disk
     */
    private long calculateFileSize() {
        if (filePath == null) return 0;
        File file = new File(filePath);
        return file.exists() ? file.length() : 0;
    }
    
    /**
     * Calculate size from base64 content
     */
    private static long calculateBase64Size(String base64Content) {
        if (base64Content == null) return 0;
        // Base64 encoding increases size by ~33%, so decode size is roughly 3/4
        return (long) (base64Content.length() * 0.75);
    }
    
    /**
     * Determine content type from file extension
     */
    private String determineContentType() {
        String extension = getFileExtension();
        
        switch (extension) {
            // Images
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "svg":
                return "image/svg+xml";
                
            // Documents
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                
            // Text
            case "txt":
                return "text/plain";
            case "csv":
                return "text/csv";
            case "html":
            case "htm":
                return "text/html";
                
            // Archives
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            case "7z":
                return "application/x-7z-compressed";
                
            default:
                return "application/octet-stream";
        }
    }
    
    @Override
    public String toString() {
        return "EmailAttachment{" +
                "fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", contentType='" + contentType + '\'' +
                ", disposition='" + disposition + '\'' +
                ", contentId='" + contentId + '\'' +
                ", fileSize=" + fileSize + " (" + getFormattedFileSize() + ")" +
                ", description='" + description + '\'' +
                ", inline=" + inline +
                ", createdAt=" + createdAt +
                ", hasBase64Content=" + (base64Content != null) +
                ", exists=" + exists() +
                ", valid=" + isValid() +
                '}';
    }
}