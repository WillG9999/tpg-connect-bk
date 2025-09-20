package com.tpg.connect.services.email;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.tpg.connect.config.EmailConfig;
import com.tpg.connect.model.email.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AWSEmailProvider implements EmailProvider {

    @Autowired
    private EmailConfig emailConfig;

    private AmazonSimpleEmailService getSESClient() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(
            emailConfig.getAwsAccessKey(), 
            emailConfig.getAwsSecretKey()
        );
        
        return AmazonSimpleEmailServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
            .withRegion(Regions.fromName(emailConfig.getAwsRegion()))
            .build();
    }

    @Override
    public void sendEmail(EmailRequest emailRequest) throws Exception {
        AmazonSimpleEmailService client = getSESClient();
        
        Destination destination = new Destination().withToAddresses(emailRequest.getTo());
        
        Content subject = new Content().withData(emailRequest.getSubject());
        
        Content htmlContent = null;
        Content textContent = null;
        
        if (emailRequest.isHtml() && emailRequest.getHtmlContent() != null) {
            htmlContent = new Content().withData(emailRequest.getHtmlContent());
        }
        if (emailRequest.getTextContent() != null) {
            textContent = new Content().withData(emailRequest.getTextContent());
        }
        
        Body body = new Body();
        if (htmlContent != null) {
            body.withHtml(htmlContent);
        }
        if (textContent != null) {
            body.withText(textContent);
        }
        
        Message message = new Message()
            .withSubject(subject)
            .withBody(body);
        
        SendEmailRequest sendEmailRequest = new SendEmailRequest()
            .withSource(emailConfig.getFromEmail())
            .withDestination(destination)
            .withMessage(message);
        
        client.sendEmail(sendEmailRequest);
    }

    @Override
    public void sendTemplatedEmail(EmailRequest emailRequest) throws Exception {
        AmazonSimpleEmailService client = getSESClient();
        
        Destination destination = new Destination().withToAddresses(emailRequest.getTo());
        
        Map<String, String> templateData = new HashMap<>();
        if (emailRequest.getTemplateData() != null) {
            for (Map.Entry<String, Object> entry : emailRequest.getTemplateData().entrySet()) {
                templateData.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        
        String templateDataJson = convertMapToJson(templateData);
        
        SendTemplatedEmailRequest sendTemplatedEmailRequest = new SendTemplatedEmailRequest()
            .withSource(emailConfig.getFromEmail())
            .withDestination(destination)
            .withTemplate(emailRequest.getTemplateType().getTemplateId())
            .withTemplateData(templateDataJson);
        
        client.sendTemplatedEmail(sendTemplatedEmailRequest);
    }

    @Override
    public boolean validateConfiguration() {
        return emailConfig.getAwsAccessKey() != null && 
               !emailConfig.getAwsAccessKey().isEmpty() &&
               emailConfig.getAwsSecretKey() != null &&
               !emailConfig.getAwsSecretKey().isEmpty() &&
               emailConfig.getFromEmail() != null &&
               !emailConfig.getFromEmail().isEmpty();
    }

    @Override
    public String getProviderName() {
        return "AWS SES";
    }

    private String convertMapToJson(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}