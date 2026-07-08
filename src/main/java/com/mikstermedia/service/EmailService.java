package com.mikstermedia.service;

import com.mikstermedia.model.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.mail.from}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendWelcomeEmail(Member member) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("RESEND_API_KEY is not set. Skipping welcome email to {}", member.getEmail());
            return;
        }

        try {
            String htmlContent = "<h2>Welcome to Mikster Media, " + member.getDisplayName() + "!</h2>" +
                    "<p>Thank you for joining our community.</p>" +
                    "<h3>What We Do</h3>" +
                    "<p>Mikster Media is the premier platform to discover, rate, and track the world's finest AI-generated music. From Suno symphonies to Udio anthems — all in one place.</p>" +
                    "<h3>How Top Tracks Are Ranked</h3>" +
                    "<p>Our unique algorithm ranks tracks based on a combination of global platform statistics (Spotify pop score, Last.fm listeners, YouTube views, TikTok plays, Suno/Udio likes) and community-driven local upvotes. " +
                    "As a member, you have the power to upvote your favorite tracks once per day to help them climb the charts!</p>" +
                    "<p>We're excited to have you on board.</p>" +
                    "<p>Cheers,<br>The Mikster Media Team</p>";

            sendResendEmail(member.getEmail(), "Welcome to Mikster Media AI Music!", htmlContent);
            log.info("Welcome email sent to {} via Resend API", member.getEmail());

        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", member.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendAdminNotification(Member member) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("RESEND_API_KEY is not set. Skipping admin notification for {}", member.getEmail());
            return;
        }

        try {
            String content = "<p>A new member has joined Mikster Media!</p>" +
                    "<ul>" +
                    "<li><strong>Name:</strong> " + member.getDisplayName() + "</li>" +
                    "<li><strong>Email:</strong> " + member.getEmail() + "</li>" +
                    "<li><strong>Auth Provider:</strong> " + member.getAuthProvider() + "</li>" +
                    "<li><strong>Primary AI Tool:</strong> " + (member.getPrimaryAiTool() != null ? member.getPrimaryAiTool() : "Not specified") + "</li>" +
                    "<li><strong>Genre Interest:</strong> " + (member.getGenreInterest() != null ? member.getGenreInterest() : "Not specified") + "</li>" +
                    "</ul>";

            sendResendEmail(adminEmail, "New Member Signup: " + member.getDisplayName(), content);
            log.info("Admin notification sent for new member: {} via Resend API", member.getEmail());

        } catch (Exception e) {
            log.error("Failed to send admin notification for member {}: {}", member.getEmail(), e.getMessage());
        }
    }

    private void sendResendEmail(String to, String subject, String htmlContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> payload = Map.of(
                "from", fromEmail,
                "to", List.of(to),
                "subject", subject,
                "html", htmlContent
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.exchange("https://api.resend.com/emails", HttpMethod.POST, request, String.class);
    }
}
