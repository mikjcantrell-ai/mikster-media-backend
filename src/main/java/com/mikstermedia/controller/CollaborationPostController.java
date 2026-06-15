package com.mikstermedia.controller;

import com.mikstermedia.model.CollaborationPost;
import com.mikstermedia.repository.CollaborationPostRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/collabs")
public class CollaborationPostController {

    private final CollaborationPostRepository repo;

    public CollaborationPostController(CollaborationPostRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<CollaborationPost> getActivePosts() {
        return repo.findByActiveTrueAndCreatedAtAfterOrderByCreatedAtDesc(java.time.LocalDateTime.now().minusDays(10));
    }

    @PostMapping
    public ResponseEntity<CollaborationPost> createPost(@RequestBody CollaborationPost post) {
        post.setCreatedAt(java.time.LocalDateTime.now());
        post.setActive(true);
        CollaborationPost saved = repo.save(post);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/expire")
    public ResponseEntity<Void> expirePost(@PathVariable Long id) {
        return repo.findById(id).map(post -> {
            post.setActive(false);
            repo.save(post);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
