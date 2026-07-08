package com.mikstermedia.controller;

import com.mikstermedia.dto.AdminEmailDTO;
import com.mikstermedia.model.Member;
import com.mikstermedia.repository.MemberRepository;
import com.mikstermedia.service.EmailService;
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

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmailBlast(@RequestBody AdminEmailDTO dto) {
        List<String> targetEmails;

        if (dto.isSendToAll()) {
            targetEmails = memberRepository.findAll().stream()
                    .map(Member::getEmail)
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

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Email blast initiated to " + targetEmails.size() + " recipients."
        ));
    }
}
