package com.mikstermedia.controller;

import com.mikstermedia.dto.AdminEmailDTO;
import com.mikstermedia.model.Member;
import com.mikstermedia.model.EmailBlast;
import com.mikstermedia.repository.MemberRepository;
import com.mikstermedia.repository.EmailBlastRepository;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.service.EmailService;
import com.mikstermedia.service.TrackService;
import com.mikstermedia.model.Track;
import com.mikstermedia.model.Artist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/emails")
@RequiredArgsConstructor
@Slf4j
public class AdminEmailController {

    private final EmailService emailService;
    private final MemberRepository memberRepository;
    private final EmailBlastRepository emailBlastRepository;
    private final TrackService trackService;
    private final ArtistRepository artistRepository;

    @GetMapping
    public ResponseEntity<List<EmailBlast>> getEmailHistory() {
        return ResponseEntity.ok(emailBlastRepository.findAllByOrderBySentAtDesc());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmailBlast(@PathVariable Long id) {
        if (!emailBlastRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        emailBlastRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Email blast deleted"));
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmailBlast(@RequestBody AdminEmailDTO dto) {
        List<String> targetEmails;

        if ("ALL".equalsIgnoreCase(dto.getRecipientMode())) {
            targetEmails = memberRepository.findAll().stream()
                    .map(Member::getEmail)
                    .collect(Collectors.toList());
        } else if ("NEWSLETTER".equalsIgnoreCase(dto.getRecipientMode())) {
            targetEmails = memberRepository.findAll().stream()
                    .filter(Member::isNewsletterOptIn)
                    .map(Member::getEmail)
                    .collect(Collectors.toList());
        } else if ("OTHER".equalsIgnoreCase(dto.getRecipientMode())) {
            targetEmails = java.util.Arrays.stream(dto.getCustomEmails().split(","))
                    .map(String::trim)
                    .filter(email -> !email.isEmpty())
                    .collect(Collectors.toList());
        } else {
            targetEmails = memberRepository.findAllById(dto.getMemberIds()).stream()
                    .map(Member::getEmail)
                    .collect(Collectors.toList());
        }

        if (targetEmails.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No recipients selected"));
        }

        emailService.sendCustomEmailBlast(targetEmails, dto.getSubject(), dto.getBody());

        EmailBlast logEntry = new EmailBlast();
        logEntry.setSubject(dto.getSubject());
        logEntry.setBody(dto.getBody());
        logEntry.setRecipientCount(targetEmails.size());
        emailBlastRepository.save(logEntry);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Email blast initiated to " + targetEmails.size() + " recipients."
        ));
    }

    @PostMapping("/notify-featured-tracks")
    public ResponseEntity<Map<String, Object>> notifyFeaturedTracks() {
        List<Track> featuredTracks = trackService.getFeaturedTracks();
        int sentCount = 0;

        for (Track track : featuredTracks) {
            if (track.getCreatorEmail() != null && !track.getCreatorEmail().isBlank()) {
                emailService.sendFeaturedTrackEmail(track.getCreatorEmail(), track.getCreator(), track.getTitle(), track.getId());
                sentCount++;
            } else if (track.getCreator() != null && !track.getCreator().isBlank()) {
                artistRepository.findByNameIgnoreCase(track.getCreator()).ifPresent(artist -> {
                    if (artist.getEmail() != null && !artist.getEmail().isBlank()) {
                        emailService.sendFeaturedTrackEmail(artist.getEmail(), artist.getName(), track.getTitle(), track.getId());
                    }
                });
                sentCount++; // Counting how many artists we attempted to match
            }
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Notified featured tracks. Processed " + sentCount + " featured tracks."
        ));
    }
}
