package com.tpg.connect.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Email Client
 * 
 * Handles email client settings for various providers including
 * SMTP, SendGrid, AWS SES with authentication and connection settings.
 */
@Configuration
@ConfigurationProperties(prefix = "email")
public class EmailClientConfig {
    
    /**
     * Whether email sending is enabled
     */
    private boolean enabled = false;
    
    /**
     * Email provider: "smtp", "sendgrid", "ses"
     */
    private String provider = "smtp";
    
    /**
     * Default sender email address
     */
    private String fromEmail;
    
    /**
     * Default sender name
     */
    private String fromName;
    
    /**
     * SMTP server host
     */
    private String smtpHost = "localhost";
    
    /**
     * SMTP server port
     */
    private int smtpPort = 587;
    
    /**
     * Username for authentication
     */
    private String username;
    
    /**
     * Password for authentication
     */
    private String password;
    
    /**
     * Enable SMTP authentication
     */
    private boolean authenticationEnabled = true;
    
    /**
     * Enable STARTTLS
     */
    private boolean startTlsEnabled = true;
    
    /**
     * Enable SSL/TLS
     */
    private boolean sslEnabled = false;
    
    /**
     * AWS region (for SES)
     */
    private String awsRegion = "us-east-1";
    
    /**
     * AWS access key (for SES)
     */
    private String awsAccessKey;
    
    /**
     * AWS secret key (for SES)
     */
    private String awsSecretKey;
    
    /**
     * SendGrid API key
     */
    private String sendGridApiKey;
    
    /**
     * Connection timeout in milliseconds
     */
    private int connectionTimeout = 30000;
    
    /**
     * Read timeout in milliseconds
     */
    private int readTimeout = 30000;
    
    /**
     * Enable debug mode
     */
    private boolean debugEnabled = false;
    
    /**
     * Maximum number of retry attempts
     */
    private int maxRetries = 3;
    
    /**
     * Retry delay in milliseconds
     */
    private long retryDelay = 1000;
    
    /**
     * Maximum batch size for bulk emails
     */
    private int maxBatchSize = 50;
    
    /**
     * Enable email templates
     */
    private boolean templatesEnabled = true;
    
    /**
     * Default email template directory
     */
    private String templateDirectory = "templates/email";
    
    /**
     * Enable email tracking
     */
    private boolean trackingEnabled = false;
    
    /**
     * Email bounce handling webhook URL
     */
    private String bounceWebhookUrl;
    
    /**
     * Email click tracking webhook URL
     */
    private String clickWebhookUrl;
    
    // Getters and Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
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
    
    public String getSmtpHost() {
        return smtpHost;
    }
    
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }
    
    public int getSmtpPort() {
        return smtpPort;
    }
    
    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }
    
    public void setAuthenticationEnabled(boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }
    
    public boolean isStartTlsEnabled() {
        return startTlsEnabled;
    }
    
    public void setStartTlsEnabled(boolean startTlsEnabled) {
        this.startTlsEnabled = startTlsEnabled;
    }
    
    public boolean isSslEnabled() {
        return sslEnabled;
    }
    
    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }
    
    public String getAwsRegion() {
        return awsRegion;
    }
    
    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }
    
    public String getAwsAccessKey() {
        return awsAccessKey;
    }
    
    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }
    
    public String getAwsSecretKey() {
        return awsSecretKey;
    }
    
    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }
    
    public String getSendGridApiKey() {
        return sendGridApiKey;
    }
    
    public void setSendGridApiKey(String sendGridApiKey) {
        this.sendGridApiKey = sendGridApiKey;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
    
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public long getRetryDelay() {
        return retryDelay;
    }
    
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }
    
    public int getMaxBatchSize() {
        return maxBatchSize;
    }
    
    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }
    
    public boolean isTemplatesEnabled() {
        return templatesEnabled;
    }
    
    public void setTemplatesEnabled(boolean templatesEnabled) {
        this.templatesEnabled = templatesEnabled;
    }
    
    public String getTemplateDirectory() {
        return templateDirectory;
    }
    
    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }
    
    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }
    
    public void setTrackingEnabled(boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }
    
    public String getBounceWebhookUrl() {
        return bounceWebhookUrl;
    }
    
    public void setBounceWebhookUrl(String bounceWebhookUrl) {
        this.bounceWebhookUrl = bounceWebhookUrl;
    }
    
    public String getClickWebhookUrl() {
        return clickWebhookUrl;
    }
    
    public void setClickWebhookUrl(String clickWebhookUrl) {
        this.clickWebhookUrl = clickWebhookUrl;
    }
    
    // Utility methods
    
    /**
     * Get effective host based on provider
     */
    public String getEffectiveHost() {
        switch (provider.toLowerCase()) {
            case "sendgrid":
                return "smtp.sendgrid.net";
            case "ses":
                return "email-smtp." + awsRegion + ".amazonaws.com";
            default:
                return smtpHost;
        }
    }
    
    /**
     * Get effective port based on provider
     */
    public int getEffectivePort() {
        switch (provider.toLowerCase()) {
            case "sendgrid":
            case "ses":
                return 587;
            default:
                return smtpPort;
        }
    }
    
    /**
     * Get effective username based on provider
     */
    public String getEffectiveUsername() {
        switch (provider.toLowerCase()) {
            case "sendgrid":
                return "apikey";
            case "ses":
                return awsAccessKey;
            default:
                return username;
        }
    }
    
    /**
     * Get effective password based on provider
     */
    public String getEffectivePassword() {
        switch (provider.toLowerCase()) {
            case "sendgrid":
                return sendGridApiKey;
            case "ses":
                return awsSecretKey;
            default:
                return password;
        }
    }
    
    /**
     * Check if provider is configured
     */
    public boolean isProviderConfigured() {
        switch (provider.toLowerCase()) {
            case "sendgrid":
                return sendGridApiKey != null && !sendGridApiKey.trim().isEmpty();
            case "ses":
                return awsAccessKey != null && !awsAccessKey.trim().isEmpty() &&
                       awsSecretKey != null && !awsSecretKey.trim().isEmpty();
            case "smtp":
            default:
                return smtpHost != null && !smtpHost.trim().isEmpty();
        }
    }
    
    /**
     * Validate configuration
     */
    public boolean isValid() {
        return enabled &&
               fromEmail != null && !fromEmail.trim().isEmpty() &&
               isProviderConfigured();
    }
    
    @Override
    public String toString() {
        return "EmailClientConfig{" +
                "enabled=" + enabled +
                ", provider='" + provider + '\'' +
                ", fromEmail='" + fromEmail + '\'' +
                ", fromName='" + fromName + '\'' +
                ", smtpHost='" + smtpHost + '\'' +
                ", smtpPort=" + smtpPort +
                ", username='" + (username != null ? "[CONFIGURED]" : "[NOT SET]") + '\'' +
                ", password='" + (password != null ? "[CONFIGURED]" : "[NOT SET]") + '\'' +
                ", authenticationEnabled=" + authenticationEnabled +
                ", startTlsEnabled=" + startTlsEnabled +
                ", sslEnabled=" + sslEnabled +
                ", awsRegion='" + awsRegion + '\'' +
                ", awsAccessKey='" + (awsAccessKey != null ? "[CONFIGURED]" : "[NOT SET]") + '\'' +
                ", awsSecretKey='" + (awsSecretKey != null ? "[CONFIGURED]" : "[NOT SET]") + '\'' +
                ", sendGridApiKey='" + (sendGridApiKey != null ? "[CONFIGURED]" : "[NOT SET]") + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", readTimeout=" + readTimeout +
                ", debugEnabled=" + debugEnabled +
                ", maxRetries=" + maxRetries +
                ", retryDelay=" + retryDelay +
                ", maxBatchSize=" + maxBatchSize +
                ", templatesEnabled=" + templatesEnabled +
                ", templateDirectory='" + templateDirectory + '\'' +
                ", trackingEnabled=" + trackingEnabled +
                '}';
    }
}