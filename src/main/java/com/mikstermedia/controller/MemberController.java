package com.mikstermedia.controller;

import com.mikstermedia.dto.MemberDTO;
import com.mikstermedia.model.Member;
import com.mikstermedia.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for community membership.
 *
 * POST /api/members/join  — public, no auth required (permitted in SecurityConfig)
 * GET  /api/members/count — public, returns total member count for homepage stat
 * GET  /api/members       — admin only (enforced by SecurityConfig)
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    /** Public join endpoint — called from the /join page. */
    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> join(@Valid @RequestBody MemberDTO dto) {
        Member m = memberService.join(dto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Welcome to the community, " + m.getDisplayName() + "!",
            "tier",    m.getMembershipTier(),
            "email",   m.getEmail(),
            "displayName", m.getDisplayName()
        ));
    }

    /** Public login endpoint */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody com.mikstermedia.dto.MemberLoginDTO dto) {
        Member m = memberService.login(dto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Welcome back, " + m.getDisplayName() + "!",
            "tier",    m.getMembershipTier(),
            "email",   m.getEmail(),
            "displayName", m.getDisplayName()
        ));
    }

    @PostMapping("/oauth2")
    public ResponseEntity<Map<String, Object>> oauth2Login(@Valid @RequestBody com.mikstermedia.dto.OAuth2LoginDTO dto) {
        Member m = memberService.oauth2Login(dto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Welcome, " + m.getDisplayName() + "!",
            "tier",    m.getMembershipTier(),
            "email",   m.getEmail(),
            "displayName", m.getDisplayName()
        ));
    }

    /** Change membership tier */
    @PostMapping("/change-tier")
    public ResponseEntity<Map<String, Object>> changeTier(@Valid @RequestBody com.mikstermedia.dto.ChangeTierDTO dto) {
        Member m = memberService.changeTier(dto);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Your membership tier has been updated to " + m.getMembershipTier() + "!",
            "tier",    m.getMembershipTier(),
            "email",   m.getEmail(),
            "displayName", m.getDisplayName()
        ));
    }

    /** Public member count — used on the homepage hero stats. */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("count", memberService.count()));
    }

    /** Admin: full member list. Secured by SecurityConfig (GET /api/** is public,
     *  but admin dashboard uses this via auth interceptor). */
    @GetMapping
    public ResponseEntity<List<Member>> getAll() {
        return ResponseEntity.ok(memberService.getAll());
    }
}
