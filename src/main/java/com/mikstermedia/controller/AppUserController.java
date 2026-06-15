package com.mikstermedia.controller;

import com.mikstermedia.dto.UserRequest;
import com.mikstermedia.model.AppUser;
import com.mikstermedia.repository.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AppUserController — admin-only CRUD for platform users.
 *
 * Security (enforced by SecurityConfig):
 *   GET  /api/users        → ADMIN only (list contains no password hashes due to @JsonIgnore)
 *   POST/PUT/DELETE        → ADMIN only
 *
 * Password handling:
 *   - POST: password required, BCrypt-hashed before storage
 *   - PUT:  password optional — omit to keep existing hash
 */
@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserRepository repo;
    private final PasswordEncoder encoder;

    public AppUserController(AppUserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    /** List all users — password hash excluded via @JsonIgnore on entity. */
    @GetMapping
    public List<AppUser> getAll() {
        return repo.findAll();
    }

    /** Get the currently authenticated admin user. */
    @GetMapping("/me")
    public ResponseEntity<AppUser> getCurrentUser(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return repo.findByUsernameIgnoreCase(principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** Create a new user. Password is required and will be BCrypt-hashed. */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required for new users"));
        }
        if (repo.existsByUsernameIgnoreCase(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists: " + req.getUsername()));
        }

        AppUser user = new AppUser();
        user.setUsername(req.getUsername().trim());
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRole(req.getRole() != null ? req.getRole().toUpperCase() : "USER");
        user.setDisplayName(req.getDisplayName());
        user.setActive(req.getActive() != null ? req.getActive() : true);

        return ResponseEntity.ok(repo.save(user));
    }

    /** Update user. Password left unchanged if not provided. */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserRequest req) {
        return repo.findById(id).map(user -> {
            if (req.getUsername() != null && !req.getUsername().isBlank()) {
                // Check for username conflict (excluding self)
                repo.findByUsernameIgnoreCase(req.getUsername())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> { throw new RuntimeException("Username already taken"); });
                user.setUsername(req.getUsername().trim());
            }
            // Only update password if a new one is provided
            if (req.getPassword() != null && !req.getPassword().isBlank()) {
                user.setPasswordHash(encoder.encode(req.getPassword()));
            }
            if (req.getEmail() != null)       user.setEmail(req.getEmail());
            if (req.getRole() != null)        user.setRole(req.getRole().toUpperCase());
            if (req.getDisplayName() != null) user.setDisplayName(req.getDisplayName());
            if (req.getActive() != null)      user.setActive(req.getActive());

            return ResponseEntity.ok(repo.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Delete user. Prevents deleting the last ADMIN account. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repo.findById(id).map(user -> {
            // Guard: don't delete if it's the only admin
            if ("ADMIN".equals(user.getRole())) {
                long adminCount = repo.findByRole("ADMIN").size();
                if (adminCount <= 1) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cannot delete the last admin account"));
                }
            }
            repo.deleteById(id);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Reset a user's password — convenience endpoint. */
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password is required"));
        }
        return repo.findById(id).map(user -> {
            user.setPasswordHash(encoder.encode(newPassword));
            return ResponseEntity.ok(Map.of("message", "Password updated for user: " + user.getUsername()));
        }).orElse(ResponseEntity.notFound().build());
    }
}
