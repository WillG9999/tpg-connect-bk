package com.tpg.connect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${email.provider:smtp}")
    private String emailProvider;

    @Value("${email.from:noreply@connectapp.com}")
    private String fromEmail;

    @Value("${email.smtp.host:smtp.gmail.com}")
    private String smtpHost;

    @Value("${email.smtp.port:587}")
    private int smtpPort;

    @Value("${email.smtp.username:}")
    private String smtpUsername;

    @Value("${email.smtp.password:}")
    private String smtpPassword;

    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;

    @Value("${aws.ses.region:eu-west-1}")
    private String awsRegion;

    @Value("${aws.ses.access.key:}")
    private String awsAccessKey;

    @Value("${aws.ses.secret.key:}")
    private String awsSecretKey;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpHost);
        mailSender.setPort(smtpPort);
        mailSender.setUsername(smtpUsername);
        mailSender.setPassword(smtpPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }

    public String getEmailProvider() {
        return emailProvider;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getSendGridApiKey() {
        return sendGridApiKey;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }
}