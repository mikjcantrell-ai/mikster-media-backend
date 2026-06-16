package com.mikstermedia.service;

import com.mikstermedia.dto.SubmissionDTO;
import com.mikstermedia.model.Member;
import com.mikstermedia.model.PendingSubmission;
import com.mikstermedia.repository.MemberRepository;
import com.mikstermedia.repository.PendingSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business-logic layer for creator music submissions.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * URL DOMAIN VALIDATION
 * ─────────────────────────────────────────────────────────────────────────────
 * Before persisting any submission, {@link #validateStreamUrl} checks that the
 * provided URL matches the expected domain for the declared platform:
 *
 *   Spotify  → must contain "open.spotify.com"
 *   YouTube  → must contain "youtube.com" OR "youtu.be"
 *   Apple    → must contain "music.apple.com"
 *
 * If the URL does not match the declared platform, an {@link IllegalArgumentException}
 * is thrown and the controller returns a 400 Bad Request to Angular.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubmissionService {

    private final PendingSubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PendingSubmission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PendingSubmission getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WRITE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates the stream URL against the declared platform, then persists
     * the submission to the {@code pending_submissions} table.
     *
     * @param dto validated SubmissionDTO from the controller
     * @return the saved PendingSubmission entity
     * @throws IllegalArgumentException when the URL domain does not match the platform
     */
    public PendingSubmission createSubmission(SubmissionDTO dto) {
        // Domain whitelist check — enforced before any persistence
        validateStreamUrl(dto.getStreamUrl(), dto.getPlatformType());

        PendingSubmission submission = new PendingSubmission();
        submission.setTrackTitle(dto.getTrackTitle());
        submission.setArtistName(dto.getArtistName());
        submission.setStreamUrl(dto.getStreamUrl());
        submission.setPlatformType(dto.getPlatformType());
        submission.setToolsDeclared(dto.getToolsDeclared());
        submission.setSubmitterEmail(dto.getSubmitterEmail());
        submission.setVideoUrl(dto.getVideoUrl());
        submission.setArtistWebsite(dto.getArtistWebsite());
        
        memberRepository.findByEmailIgnoreCase(dto.getSubmitterEmail()).ifPresent(member -> {
            if ("PRODUCER".equalsIgnoreCase(member.getMembershipTier())) {
                submission.setPriority(true);
            }
        });
        // submissionDate is set automatically via @PrePersist

        log.info("New submission ingested: '{}' by {}", dto.getTrackTitle(), dto.getArtistName());
        return submissionRepository.save(submission);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL — URL DOMAIN VALIDATOR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks that the provided URL belongs to the expected domain for the
     * selected platform.
     *
     * @param url          the URL submitted by the creator
     * @param platformType "Spotify", "YouTube", or "Apple"
     * @throws IllegalArgumentException if the domain does not match the platform
     */
    public void validateStreamUrl(String url, String platformType) {
        if (url == null || platformType == null) {
            throw new IllegalArgumentException("URL and platform type cannot be null");
        }

        String lowerUrl = url.toLowerCase();

        boolean valid = switch (platformType) {
            case "Spotify" -> lowerUrl.contains("open.spotify.com");
            case "YouTube" -> lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be");
            case "Apple"   -> lowerUrl.contains("music.apple.com");
            default        -> throw new IllegalArgumentException("Unknown platform: " + platformType);
        };

        if (!valid) {
            log.warn("URL domain mismatch — platform={}, url={}", platformType, url);
            throw new IllegalArgumentException(
                String.format("The URL '%s' does not match the expected domain for platform '%s'.",
                              url, platformType)
            );
        }

        log.debug("URL validated OK for platform={}", platformType);
    }

    public void deleteSubmission(Long id) {
        submissionRepository.deleteById(id);
        log.info("Submission {} deleted by admin.", id);
    }
}
