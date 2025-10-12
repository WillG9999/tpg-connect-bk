package com.tpg.connect.services.email;

import com.tpg.connect.config.EmailConfig;
import com.tpg.connect.model.email.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
public class SMTPEmailProvider implements EmailProvider {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailConfig emailConfig;

    @Override
    public void sendEmail(EmailRequest emailRequest) throws Exception {
        if (emailRequest.isHtml()) {
            sendHtmlEmail(emailRequest);
        } else {
            sendTextEmail(emailRequest);
        }
    }

    private void sendTextEmail(EmailRequest emailRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailConfig.getFromEmail());
        message.setTo(emailRequest.getTo());
        message.setSubject(emailRequest.getSubject());
        message.setText(emailRequest.getTextContent());

        if (emailRequest.getReplyTo() != null) {
            message.setReplyTo(emailRequest.getReplyTo());
        }

        mailSender.send(message);
    }

    private void sendHtmlEmail(EmailRequest emailRequest) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setFrom(emailConfig.getFromEmail());
        helper.setTo(emailRequest.getTo());
        helper.setSubject(emailRequest.getSubject());

        if (emailRequest.getHtmlContent() != null) {
            helper.setText(emailRequest.getTextContent(), emailRequest.getHtmlContent());
        } else {
            helper.setText(emailRequest.getTextContent(), false);
        }

        if (emailRequest.getReplyTo() != null) {
            helper.setReplyTo(emailRequest.getReplyTo());
        }

        if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
            helper.setCc(emailRequest.getCc().toArray(new String[0]));
        }

        if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
            helper.setBcc(emailRequest.getBcc().toArray(new String[0]));
        }

        mailSender.send(mimeMessage);
    }

    @Override
    public void sendTemplatedEmail(EmailRequest emailRequest) throws Exception {
        String htmlContent = processTemplate(emailRequest);
        String textContent = stripHtml(htmlContent);

        EmailRequest processedRequest = new EmailRequest();
        processedRequest.setTo(emailRequest.getTo());
        processedRequest.setSubject(emailRequest.getSubject() != null ?
                emailRequest.getSubject() : emailRequest.getTemplateType().getDefaultSubject());
        processedRequest.setHtmlContent(htmlContent);
        processedRequest.setTextContent(textContent);
        processedRequest.setHtml(true);
        processedRequest.setReplyTo(emailRequest.getReplyTo());
        processedRequest.setCc(emailRequest.getCc());
        processedRequest.setBcc(emailRequest.getBcc());

        sendEmail(processedRequest);
    }

    @Override
    public boolean validateConfiguration() {
        return emailConfig.getFromEmail() != null &&
                !emailConfig.getFromEmail().isEmpty();
    }

    @Override
    public String getProviderName() {
        return "SMTP";
    }

    private String processTemplate(EmailRequest emailRequest) {
        String template = getTemplateContent(emailRequest.getTemplateType());

        if (emailRequest.getTemplateData() != null) {
            for (java.util.Map.Entry<String, Object> entry : emailRequest.getTemplateData().entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
            }
        }

        return template;
    }

    private String getTemplateContent(com.tpg.connect.model.email.EmailTemplate.TemplateType templateType) {
        switch (templateType) {
            case EMAIL_VERIFICATION:
                return """
                        <html>
                        <body>
                            <h2>Verify Your Connect Account</h2>
                            <p>Hi {{firstName}},</p>
                            <p>Welcome to Connect! Please verify your email address by clicking the link below:</p>
                            <p><a href="{{verificationLink}}" style="background-color: #FF6B6B; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Verify Email</a></p>
                            <p>Or copy and paste this link into your browser: {{verificationLink}}</p>
                            <p>This link will expire in 24 hours.</p>
                            <p>If you didn't create this account, please ignore this email.</p>
                            <p>Best regards,<br>The Connect Team</p>
                        </body>
                        </html>
                        """;
            case PASSWORD_RESET:
                return """
                        <html>
                        <body>
                            <h2>Reset Your Connect Password</h2>
                            <p>Hi,</p>
                            <p>We received a request to reset your password. Click the link below to create a new password:</p>
                            <p><a href="{{resetLink}}" style="background-color: #FF6B6B; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Reset Password</a></p>
                            <p>Or copy and paste this link into your browser: {{resetLink}}</p>
                            <p>This link will expire in 1 hour.</p>
                            <p>If you didn't request this reset, please ignore this email and your password will remain unchanged.</p>
                            <p>Best regards,<br>The Connect Team</p>
                        </body>
                        </html>
                        """;
            case WELCOME:
                return """
                        <html>
                        <body>
                            <h2>Welcome to Connect!</h2>
                            <p>Hi {{firstName}},</p>
                            <p>Your email has been verified and your Connect account is now active!</p>
                            <p>Start discovering amazing people in your area. Your perfect match is waiting!</p>
                            <p>Get started by completing your profile and uploading your best photos.</p>
                            <p>Happy matching!<br>The Connect Team</p>
                        </body>
                        </html>
                        """;
            case SUBSCRIPTION_CONFIRMATION:
                return """
                        <html>
                        <body>
                            <h2>Connect Premium Subscription Active</h2>
                            <p>Hi,</p>
                            <p>Your Connect Premium subscription is now active!</p>
                            <p><strong>Plan:</strong> {{plan}}</p>
                            <p><strong>Duration:</strong> {{duration}} months</p>
                            <p><strong>Total Amount:</strong> £{{totalAmount}}</p>
                            <p>You now have access to all premium features. Enjoy enhanced matching and discovery!</p>
                            <p>Thank you for choosing Connect Premium!</p>
                            <p>Best regards,<br>The Connect Team</p>
                        </body>
                        </html>
                        """;
            default:
                return """
                        <html>
                        <body>
                            <p>{{content}}</p>
                            <p>Best regards,<br>The Connect Team</p>
                        </body>
                        </html>
                        """;
        }
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }
}