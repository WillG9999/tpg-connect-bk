package com.tpg.connect.services.email;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.tpg.connect.config.EmailConfig;
import com.tpg.connect.model.email.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class SendGridEmailProvider implements EmailProvider {

    @Autowired
    private EmailConfig emailConfig;

    @Override
    public void sendEmail(EmailRequest emailRequest) throws IOException {
        SendGrid sg = new SendGrid(emailConfig.getSendGridApiKey());
        
        Email from = new Email(emailConfig.getFromEmail());
        Email to = new Email(emailRequest.getTo());
        
        Content content = new Content(
            emailRequest.isHtml() ? "text/html" : "text/plain",
            emailRequest.isHtml() ? emailRequest.getHtmlContent() : emailRequest.getTextContent()
        );
        
        Mail mail = new Mail(from, emailRequest.getSubject(), to, content);
        
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        
        Response response = sg.api(request);
        
        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("SendGrid email failed with status: " + response.getStatusCode() + 
                                     ", body: " + response.getBody());
        }
    }

    @Override
    public void sendTemplatedEmail(EmailRequest emailRequest) throws IOException {
        SendGrid sg = new SendGrid(emailConfig.getSendGridApiKey());
        
        Email from = new Email(emailConfig.getFromEmail());
        Email to = new Email(emailRequest.getTo());
        
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(emailRequest.getSubject());
        
        if (emailRequest.getTemplateType() != null) {
            mail.setTemplateId(emailRequest.getTemplateType().getTemplateId());
        }
        
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        
        if (emailRequest.getTemplateData() != null) {
            for (Map.Entry<String, Object> entry : emailRequest.getTemplateData().entrySet()) {
                personalization.addDynamicTemplateData(entry.getKey(), entry.getValue());
            }
        }
        
        mail.addPersonalization(personalization);
        
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        
        Response response = sg.api(request);
        
        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("SendGrid templated email failed with status: " + response.getStatusCode() + 
                                     ", body: " + response.getBody());
        }
    }

    @Override
    public boolean validateConfiguration() {
        return emailConfig.getSendGridApiKey() != null && 
               !emailConfig.getSendGridApiKey().isEmpty() &&
               emailConfig.getFromEmail() != null &&
               !emailConfig.getFromEmail().isEmpty();
    }

    @Override
    public String getProviderName() {
        return "SendGrid";
    }
}