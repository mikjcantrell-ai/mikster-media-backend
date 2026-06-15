package com.mikstermedia.controller;

import com.mikstermedia.dto.InquiryDTO;
import com.mikstermedia.model.Inquiry;
import com.mikstermedia.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for contact-form {@link Inquiry} operations.
 *
 * <p>Base path: {@code /api/inquiries}
 *
 * <ul>
 *   <li>GET  /api/inquiries           — admin: list all</li>
 *   <li>GET  /api/inquiries/unread-count — admin: unread badge count</li>
 *   <li>POST /api/inquiries            — public: submit contact form</li>
 *   <li>PATCH /api/inquiries/{id}/read — admin: mark read</li>
 *   <li>PATCH /api/inquiries/{id}/reply — admin: save reply</li>
 *   <li>DELETE /api/inquiries/{id}    — admin: delete</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /** GET /api/inquiries — admin list of all received inquiries, newest first */
    @GetMapping
    public ResponseEntity<List<Inquiry>> getAllInquiries() {
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }

    /** GET /api/inquiries/unread-count — count of unread inquiries for badge */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", inquiryService.countUnread()));
    }

    /**
     * POST /api/inquiries
     * Persists a contact-form message. Returns 201 Created with saved entity.
     */
    @PostMapping
    public ResponseEntity<Inquiry> createInquiry(@Valid @RequestBody InquiryDTO dto) {
        Inquiry saved = inquiryService.createInquiry(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** PATCH /api/inquiries/{id}/read — mark an inquiry as read */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Inquiry> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(inquiryService.markRead(id));
    }

    /** PATCH /api/inquiries/{id}/reply — save admin reply text */
    @PatchMapping("/{id}/reply")
    public ResponseEntity<Inquiry> saveReply(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String replyText = body.getOrDefault("reply", "");
        return ResponseEntity.ok(inquiryService.saveReply(id, replyText));
    }

    /** DELETE /api/inquiries/{id} — permanently remove an inquiry */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ResponseEntity.noContent().build();
    }
}
