package com.mikstermedia.controller;

import com.mikstermedia.dto.SubmissionDTO;
import com.mikstermedia.model.PendingSubmission;
import com.mikstermedia.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the "Submit Your Music" creator ingestion flow.
 *
 * <p>Base path: {@code /api/submissions}
 *
 * <p>Integration notes for Angular:
 * <ul>
 *   <li>SubmitComponent's reactive form POSTs to {@code /api/submissions}.</li>
 *   <li>A 400 Bad Request response (from URL domain validation) is caught by
 *       Angular's error interceptor and displayed as an inline form error.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /** GET /api/submissions — admin view of all pending submissions */
    @GetMapping
    public ResponseEntity<List<PendingSubmission>> getAllSubmissions() {
        return ResponseEntity.ok(submissionService.getAllSubmissions());
    }

    /** GET /api/submissions/{id} — single submission detail */
    @GetMapping("/{id}")
    public ResponseEntity<PendingSubmission> getSubmissionById(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getSubmissionById(id));
    }

    /**
     * POST /api/submissions
     * Ingests a creator's music submission after URL domain validation.
     *
     * <p>Response codes:
     * <ul>
     *   <li>201 Created — submission saved to pending_submissions table.</li>
     *   <li>400 Bad Request — Bean Validation failure OR URL domain mismatch.</li>
     * </ul>
     */
    @PostMapping
    public ResponseEntity<?> createSubmission(@Valid @RequestBody SubmissionDTO dto) {
        try {
            PendingSubmission saved = submissionService.createSubmission(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            // URL domain validation failure — return structured error for Angular
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/submissions/{id} — admin removes a pending submission */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        submissionService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }
}
