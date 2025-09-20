package com.tpg.connect.model.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String to;
    private List<String> toList;
    private String from;
    private String subject;
    private String htmlContent;
    private String textContent;
    private EmailTemplate.TemplateType templateType;
    private Map<String, Object> templateData;
    private String replyTo;
    private List<String> cc;
    private List<String> bcc;
    private boolean isHtml = true;
    
    public EmailRequest(String to, EmailTemplate.TemplateType templateType, Map<String, Object> templateData) {
        this.to = to;
        this.templateType = templateType;
        this.templateData = templateData;
        this.isHtml = true;
    }
}