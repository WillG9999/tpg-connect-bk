package com.tpg.connect.services.email;

import com.tpg.connect.model.email.EmailRequest;

public interface EmailProvider {
    
    void sendEmail(EmailRequest emailRequest) throws Exception;
    
    void sendTemplatedEmail(EmailRequest emailRequest) throws Exception;
    
    boolean validateConfiguration();
    
    String getProviderName();
}