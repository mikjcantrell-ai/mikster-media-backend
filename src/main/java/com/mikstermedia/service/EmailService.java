package com.mikstermedia.service;

import com.mikstermedia.model.Member;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(Member member) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(member.getEmail());
            helper.setSubject("Welcome to Mikster Media AI Music!");

            String htmlContent = "<h2>Welcome to Mikster Media, " + member.getDisplayName() + "!</h2>" +
                    "<p>Thank you for joining our community.</p>" +
                    "<h3>What We Do</h3>" +
                    "<p>Mikster Media is the premier platform to discover, rate, and track the world's finest AI-generated music. From Suno symphonies to Udio anthems — all in one place.</p>" +
                    "<h3>How Top Tracks Are Ranked</h3>" +
                    "<p>Our unique algorithm ranks tracks based on a combination of global platform statistics (Spotify pop score, Last.fm listeners, YouTube views, TikTok plays, Suno/Udio likes) and community-driven local upvotes. " +
                    "As a member, you have the power to upvote your favorite tracks once per day to help them climb the charts!</p>" +
                    "<p>We're excited to have you on board.</p>" +
                    "<p>Cheers,<br>The Mikster Media Team</p>";

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            log.info("Welcome email sent to {}", member.getEmail());

        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", member.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendAdminNotification(Member member) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("New Member Signup: " + member.getDisplayName());

            String content = "A new member has joined Mikster Media!\n\n" +
                    "Name: " + member.getDisplayName() + "\n" +
                    "Email: " + member.getEmail() + "\n" +
                    "Auth Provider: " + member.getAuthProvider() + "\n" +
                    "Primary AI Tool: " + (member.getPrimaryAiTool() != null ? member.getPrimaryAiTool() : "Not specified") + "\n" +
                    "Genre Interest: " + (member.getGenreInterest() != null ? member.getGenreInterest() : "Not specified");

            helper.setText(content, false);
            javaMailSender.send(message);
            log.info("Admin notification sent for new member: {}", member.getEmail());

        } catch (Exception e) {
            log.error("Failed to send admin notification for member {}: {}", member.getEmail(), e.getMessage());
        }
    }
}
