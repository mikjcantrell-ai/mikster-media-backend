package com.mikstermedia.service;

import com.mikstermedia.dto.MemberDTO;
import com.mikstermedia.model.Member;
import com.mikstermedia.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleOAuth2Service googleOAuth2Service;

    public Member oauth2Login(com.mikstermedia.dto.OAuth2LoginDTO dto) {
        if ("GOOGLE".equalsIgnoreCase(dto.getProvider())) {
            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = googleOAuth2Service.verifyIdToken(dto.getIdToken());
            if (payload == null) {
                throw new IllegalArgumentException("Invalid Google ID token.");
            }
            
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String subject = payload.getSubject();
            
            Member member = memberRepository.findByEmailIgnoreCase(email).orElse(null);
            if (member == null) {
                member = new Member();
                member.setEmail(email.toLowerCase().trim());
                member.setDisplayName(name);
                member.setAuthProvider("GOOGLE");
                member.setProviderId(subject);
                member.setMembershipTier("LISTENER");
                return memberRepository.save(member);
            } else {
                // If they exist, ensure their auth provider is set so they can login next time
                if ("LOCAL".equals(member.getAuthProvider())) {
                    member.setAuthProvider("GOOGLE");
                    member.setProviderId(subject);
                    memberRepository.save(member);
                }
                return member;
            }
        }
        throw new IllegalArgumentException("Unsupported OAuth2 provider: " + dto.getProvider());
    }

    /**
     * Registers a new community member from the /join page.
     * Throws IllegalArgumentException on duplicate email or username.
     */
    public Member join(MemberDTO dto) {
        Member member = null;
        
        if (memberRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            member = memberRepository.findByEmailIgnoreCase(dto.getEmail()).orElse(null);
            if (member != null && member.getPasswordHash() != null) {
                throw new IllegalArgumentException("That email address is already registered.");
            }
            // If they exist but have no password (legacy account), we allow them to claim/update it!
        }
        
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            Member usernameMember = memberRepository.findByUsernameIgnoreCase(dto.getUsername()).orElse(null);
            if (usernameMember != null && (member == null || !usernameMember.getId().equals(member.getId()))) {
                throw new IllegalArgumentException("That username is already taken.");
            }
        }

        if (member == null) {
            member = new Member();
        }
        
        member.setDisplayName(dto.getDisplayName().trim());
        member.setEmail(dto.getEmail().toLowerCase().trim());
        member.setUsername(dto.getUsername() != null ? dto.getUsername().trim() : null);
        member.setMembershipTier(
            dto.getMembershipTier() != null ? dto.getMembershipTier().toUpperCase() : "LISTENER");
        member.setPrimaryAiTool(dto.getPrimaryAiTool());
        member.setGenreInterest(dto.getGenreInterest());
        member.setNewsletterOptIn(dto.isNewsletterOptIn());
        
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            member.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        Member saved = memberRepository.save(member);
        log.info("New {} member joined: {} <{}>", saved.getMembershipTier(),
                saved.getDisplayName(), saved.getEmail());
        return saved;
    }

    public Member login(com.mikstermedia.dto.MemberLoginDTO dto) {
        Member member = memberRepository.findByEmailIgnoreCase(dto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        
        if (member.getPasswordHash() == null) {
            throw new IllegalArgumentException("Account not fully set up with a password. Please join again with a different email for now.");
        }
        
        if (!passwordEncoder.matches(dto.getPassword(), member.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        
        return member;
    }

    public Member changeTier(com.mikstermedia.dto.ChangeTierDTO dto) {
        Member member = memberRepository.findByEmailIgnoreCase(dto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Member not found for email: " + dto.getEmail()));
        
        member.setMembershipTier(dto.getNewTier().toUpperCase());
        Member saved = memberRepository.save(member);
        
        log.info("Member {} updated tier to {}", saved.getEmail(), saved.getMembershipTier());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Member> getAll() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long count() {
        return memberRepository.count();
    }
}
