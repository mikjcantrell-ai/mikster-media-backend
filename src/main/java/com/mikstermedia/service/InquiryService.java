package com.mikstermedia.service;

import com.mikstermedia.dto.InquiryDTO;
import com.mikstermedia.model.Inquiry;
import com.mikstermedia.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business-logic layer for contact-form {@link Inquiry} records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    @Transactional(readOnly = true)
    public List<Inquiry> getAllInquiries() {
        return inquiryRepository.findAllByOrderByReceivedDateDesc();
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return inquiryRepository.countByIsReadFalse();
    }

    /**
     * Maps the DTO to an {@link Inquiry} entity and persists it.
     * The {@code received_date} is set automatically via {@code @PrePersist}.
     */
    public Inquiry createInquiry(InquiryDTO dto) {
        Inquiry inquiry = new Inquiry();
        inquiry.setSenderName(dto.getSenderName());
        inquiry.setSenderEmail(dto.getSenderEmail());
        inquiry.setSubject(dto.getSubject());
        inquiry.setMessageBody(dto.getMessageBody());
        log.info("Inquiry received from {}", dto.getSenderEmail());
        return inquiryRepository.save(inquiry);
    }

    /** Marks the inquiry as read by the admin. */
    public Inquiry markRead(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + id));
        inquiry.setRead(true);
        return inquiryRepository.save(inquiry);
    }

    /** Saves an admin reply note against the inquiry. */
    public Inquiry saveReply(Long id, String replyText) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + id));
        inquiry.setAdminReply(replyText);
        inquiry.setRepliedAt(LocalDateTime.now());
        inquiry.setRead(true);
        log.info("Admin replied to inquiry {} from {}", id, inquiry.getSenderEmail());
        return inquiryRepository.save(inquiry);
    }

    /** Permanently removes an inquiry. */
    public void deleteInquiry(Long id) {
        if (!inquiryRepository.existsById(id)) {
            throw new IllegalArgumentException("Inquiry not found: " + id);
        }
        inquiryRepository.deleteById(id);
        log.info("Inquiry {} deleted by admin", id);
    }
}
