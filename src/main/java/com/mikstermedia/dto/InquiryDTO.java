package com.mikstermedia.dto;

import jakarta.validation.constraints.NotBlank;

public class InquiryDTO {
    @NotBlank private String senderName;
    @NotBlank private String senderEmail;
    @NotBlank private String subject;
    @NotBlank private String messageBody;

    public String getSenderName()  { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public String getSubject()     { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
}
