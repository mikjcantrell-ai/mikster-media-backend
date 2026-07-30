package com.mikstermedia.controller;

import com.mikstermedia.model.EmailBlast;
import com.mikstermedia.repository.EmailBlastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/newsletters")
@RequiredArgsConstructor
public class NewsletterController {

    private final EmailBlastRepository emailBlastRepository;

    @GetMapping
    public ResponseEntity<List<EmailBlast>> getNewsletters() {
        List<EmailBlast> newsletters = emailBlastRepository.findByTypeIgnoreCaseOrderBySentAtDesc("News Letter");
        return ResponseEntity.ok(newsletters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailBlast> getNewsletterById(@PathVariable Long id) {
        return emailBlastRepository.findById(id)
                .filter(eb -> "News Letter".equalsIgnoreCase(eb.getType()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
