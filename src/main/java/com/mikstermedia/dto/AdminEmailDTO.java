package com.mikstermedia.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminEmailDTO {
    private String subject;
    private String body;
    private String recipientMode; // ALL, NEWSLETTER, SPECIFIC, OTHER
    private List<Long> memberIds;
    private String customEmails;
}
