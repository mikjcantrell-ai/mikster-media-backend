package com.mikstermedia.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminEmailDTO {
    private String subject;
    private String body;
    private boolean sendToAll;
    private List<Long> memberIds;
}
