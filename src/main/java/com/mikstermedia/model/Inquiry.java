package com.mikstermedia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * JPA entity persisting messages submitted through the platform's contact form.
 *
 * <p>Each inquiry is stored verbatim for editorial review. Admins can mark
 * messages as read, save a reply note, and delete them from the dashboard.
 */
@Entity
@Table(name = "inquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full name of the person making contact. */
    @Column(name = "sender_name", nullable = false)
    private String senderName;

    /** Reply-to email address supplied by the sender. */
    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    /** Brief subject line (max 255 chars). */
    @Column(nullable = false)
    private String subject;

    /** Full message body; stored as TEXT to accommodate long messages. */
    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    /** Automatically set to server time when the inquiry is first persisted. */
    @Column(name = "received_date", nullable = false)
    private LocalDateTime receivedDate;

    /** Whether the admin has opened/read this inquiry. */
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    /** Admin reply text saved from the dashboard (not emailed — stored only). */
    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    /** Timestamp when the admin last saved a reply. */
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @PrePersist
    public void prePersist() {
        if (receivedDate == null) {
            receivedDate = LocalDateTime.now();
        }
    }
}
