package com.tpg.connect.client.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Model representing the alert payload for Apple Push Notifications
 * 
 * Supports all standard APNs alert fields including localization,
 * action buttons, and rich content.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApnsAlert {
    
    /**
     * Alert title
     */
    @JsonProperty("title")
    private String title;
    
    /**
     * Alert subtitle (iOS 10+)
     */
    @JsonProperty("subtitle")
    private String subtitle;
    
    /**
     * Alert body/message
     */
    @JsonProperty("body")
    private String body;
    
    /**
     * Localization key for title
     */
    @JsonProperty("title-loc-key")
    private String titleLocKey;
    
    /**
     * Localization arguments for title
     */
    @JsonProperty("title-loc-args")
    private List<String> titleLocArgs;
    
    /**
     * Localization key for subtitle
     */
    @JsonProperty("subtitle-loc-key")
    private String subtitleLocKey;
    
    /**
     * Localization arguments for subtitle
     */
    @JsonProperty("subtitle-loc-args")
    private List<String> subtitleLocArgs;
    
    /**
     * Localization key for body
     */
    @JsonProperty("loc-key")
    private String locKey;
    
    /**
     * Localization arguments for body
     */
    @JsonProperty("loc-args")
    private List<String> locArgs;
    
    /**
     * Action button localization key
     */
    @JsonProperty("action-loc-key")
    private String actionLocKey;
    
    /**
     * Launch image filename
     */
    @JsonProperty("launch-image")
    private String launchImage;
    
    // Constructors
    
    public ApnsAlert() {
    }
    
    public ApnsAlert(String title, String body) {
        this.title = title;
        this.body = body;
    }
    
    public ApnsAlert(String title, String subtitle, String body) {
        this.title = title;
        this.subtitle = subtitle;
        this.body = body;
    }
    
    // Builder methods
    
    public static ApnsAlert simple(String body) {
        ApnsAlert alert = new ApnsAlert();
        alert.body = body;
        return alert;
    }
    
    public static ApnsAlert withTitle(String title, String body) {
        return new ApnsAlert(title, body);
    }
    
    public static ApnsAlert full(String title, String subtitle, String body) {
        return new ApnsAlert(title, subtitle, body);
    }
    
    public ApnsAlert withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public ApnsAlert withSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }
    
    public ApnsAlert withBody(String body) {
        this.body = body;
        return this;
    }
    
    public ApnsAlert withTitleLocalization(String locKey, List<String> locArgs) {
        this.titleLocKey = locKey;
        this.titleLocArgs = locArgs;
        return this;
    }
    
    public ApnsAlert withSubtitleLocalization(String locKey, List<String> locArgs) {
        this.subtitleLocKey = locKey;
        this.subtitleLocArgs = locArgs;
        return this;
    }
    
    public ApnsAlert withBodyLocalization(String locKey, List<String> locArgs) {
        this.locKey = locKey;
        this.locArgs = locArgs;
        return this;
    }
    
    public ApnsAlert withActionLocalization(String actionLocKey) {
        this.actionLocKey = actionLocKey;
        return this;
    }
    
    public ApnsAlert withLaunchImage(String launchImage) {
        this.launchImage = launchImage;
        return this;
    }
    
    // Getters and Setters
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public String getTitleLocKey() {
        return titleLocKey;
    }
    
    public void setTitleLocKey(String titleLocKey) {
        this.titleLocKey = titleLocKey;
    }
    
    public List<String> getTitleLocArgs() {
        return titleLocArgs;
    }
    
    public void setTitleLocArgs(List<String> titleLocArgs) {
        this.titleLocArgs = titleLocArgs;
    }
    
    public String getSubtitleLocKey() {
        return subtitleLocKey;
    }
    
    public void setSubtitleLocKey(String subtitleLocKey) {
        this.subtitleLocKey = subtitleLocKey;
    }
    
    public List<String> getSubtitleLocArgs() {
        return subtitleLocArgs;
    }
    
    public void setSubtitleLocArgs(List<String> subtitleLocArgs) {
        this.subtitleLocArgs = subtitleLocArgs;
    }
    
    public String getLocKey() {
        return locKey;
    }
    
    public void setLocKey(String locKey) {
        this.locKey = locKey;
    }
    
    public List<String> getLocArgs() {
        return locArgs;
    }
    
    public void setLocArgs(List<String> locArgs) {
        this.locArgs = locArgs;
    }
    
    public String getActionLocKey() {
        return actionLocKey;
    }
    
    public void setActionLocKey(String actionLocKey) {
        this.actionLocKey = actionLocKey;
    }
    
    public String getLaunchImage() {
        return launchImage;
    }
    
    public void setLaunchImage(String launchImage) {
        this.launchImage = launchImage;
    }
    
    // Utility methods
    
    /**
     * Check if alert has any content
     */
    public boolean hasContent() {
        return title != null || subtitle != null || body != null || 
               titleLocKey != null || subtitleLocKey != null || locKey != null;
    }
    
    /**
     * Check if alert uses localization
     */
    public boolean isLocalized() {
        return titleLocKey != null || subtitleLocKey != null || locKey != null;
    }
    
    /**
     * Get the primary display text (title or body)
     */
    public String getPrimaryText() {
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        return body;
    }
    
    /**
     * Get the secondary display text (subtitle or null)
     */
    public String getSecondaryText() {
        return subtitle;
    }
    
    @Override
    public String toString() {
        return "ApnsAlert{" +
                "title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", body='" + body + '\'' +
                ", titleLocKey='" + titleLocKey + '\'' +
                ", titleLocArgs=" + titleLocArgs +
                ", subtitleLocKey='" + subtitleLocKey + '\'' +
                ", subtitleLocArgs=" + subtitleLocArgs +
                ", locKey='" + locKey + '\'' +
                ", locArgs=" + locArgs +
                ", actionLocKey='" + actionLocKey + '\'' +
                ", launchImage='" + launchImage + '\'' +
                '}';
    }
}