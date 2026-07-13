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
    public void sendFeaturedTrackEmail(String email, String artistName, String trackTitle, Long trackId) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("RESEND_API_KEY is not set. Skipping featured track email to {}", email);
            return;
        }

        try {
            String encodedTrackTitle = java.net.URLEncoder.encode(trackTitle, "UTF-8");
            String trackUrl = "https://mikstermedia.com/songs"; // Adjust if there is a specific track URL, e.g. /songs/trackId
            String shareText = java.net.URLEncoder.encode("So excited that my track " + trackTitle + " is currently featured on the front page of @MiksterMedia! Check it out and upvote it here: " + trackUrl, "UTF-8");
            String twitterUrl = "https://twitter.com/intent/tweet?text=" + shareText;
            String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=" + java.net.URLEncoder.encode(trackUrl, "UTF-8") + "&quote=" + shareText;

            String htmlContent = "<h2>Congratulations, " + artistName + "!</h2>" +
                    "<p>We are thrilled to let you know that your track <strong>" + trackTitle + "</strong> is currently featured on the front page of Mikster Media!</p>" +
                    "<p>Our community loves what you've created. We'd love for you to share this achievement with your fans and help spread the word about AI music.</p>" +
                    "<div style=\"background: #f4f4f4; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                    "  <p style=\"margin-top:0;\"><strong>Share this on social media:</strong></p>" +
                    "  <p style=\"font-style: italic;\">\"So excited that my track " + trackTitle + " is currently featured on the front page of @MiksterMedia! Check it out and upvote it here: " + trackUrl + "\"</p>" +
                    "  <div style=\"margin-top: 15px;\">" +
                    "    <a href=\"" + twitterUrl + "\" style=\"display:inline-block; background:#1DA1F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px; margin-right:10px;\">Share on X (Twitter)</a>" +
                    "    <a href=\"" + facebookUrl + "\" style=\"display:inline-block; background:#1877F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px;\">Share on Facebook</a>" +
                    "  </div>" +
                    "</div>" +
                    "<p>Keep up the great work!</p>" +
                    "<p>— The Mikster Media Team</p>";

            sendResendEmail(email, "Congratulations! Your track is featured on Mikster Media!", htmlContent);
            log.info("Featured track email sent to {} via Resend API", email);

        } catch (Exception e) {
            log.error("Failed to send featured track email to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void sendExtendedFeaturedTrackEmail(String email, String artistName, String trackTitle, Long trackId) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("RESEND_API_KEY is not set. Skipping extended featured track email to {}", email);
            return;
        }

        try {
            String encodedTrackTitle = java.net.URLEncoder.encode(trackTitle, "UTF-8");
            String trackUrl = "https://mikstermedia.com/songs"; 
            String shareText = java.net.URLEncoder.encode("My track " + trackTitle + " is blowing up and just got its feature extended on the front page of @MiksterMedia! Let's keep it climbing the charts, you can upvote it here: " + trackUrl, "UTF-8");
            String twitterUrl = "https://twitter.com/intent/tweet?text=" + shareText;
            String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=" + java.net.URLEncoder.encode(trackUrl, "UTF-8") + "&quote=" + shareText;

            String htmlContent = "<h2>Hi " + artistName + ",</h2>" +
                    "<p>Your track <strong>" + trackTitle + "</strong> has been performing incredibly well with the community! Because of its popularity, we have decided to <strong>extend your feature</strong> on the front page of Mikster Media.</p>" +
                    "<p>Keep the momentum going! We’d love for you to share the good news with your fans so they can continue to upvote your track.</p>" +
                    "<div style=\"background: #f4f4f4; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                    "  <p style=\"margin-top:0;\"><strong>Share this on social media:</strong></p>" +
                    "  <p style=\"font-style: italic;\">\"My track " + trackTitle + " is blowing up and just got its feature extended on the front page of @MiksterMedia! Let's keep it climbing the charts, you can upvote it here: " + trackUrl + "\"</p>" +
                    "  <div style=\"margin-top: 15px;\">" +
                    "    <a href=\"" + twitterUrl + "\" style=\"display:inline-block; background:#1DA1F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px; margin-right:10px;\">Share on X (Twitter)</a>" +
                    "    <a href=\"" + facebookUrl + "\" style=\"display:inline-block; background:#1877F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px;\">Share on Facebook</a>" +
                    "  </div>" +
                    "</div>" +
                    "<p>Congratulations again!</p>" +
                    "<p>— The Mikster Media Team</p>";

            sendResendEmail(email, "Your track is on fire! We've extended your feature on Mikster Media 🔥", htmlContent);
            log.info("Extended featured track email sent to {} via Resend API", email);

        } catch (Exception e) {
            log.error("Failed to send extended featured track email to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void sendFeaturedArtistEmail(String email, String artistName, Long artistId) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("RESEND_API_KEY is not set. Skipping featured artist email to {}", email);
            return;
        }

        try {
            String artistUrl = "https://mikstermedia.com/artists";
            String shareText = java.net.URLEncoder.encode("So excited to be featured as a top artist on @MiksterMedia this week! Check out my profile and latest AI tracks: " + artistUrl, "UTF-8");
            String twitterUrl = "https://twitter.com/intent/tweet?text=" + shareText;
            String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=" + java.net.URLEncoder.encode(artistUrl, "UTF-8") + "&quote=" + shareText;

            String htmlContent = "<h2>Congratulations, " + artistName + "!</h2>" +
                    "<p>We are thrilled to let you know that you are currently featured as a top artist on the front page of Mikster Media!</p>" +
                    "<p>Our community loves the music you've created. We'd love for you to share this achievement with your fans and help spread the word about your AI music.</p>" +
                    "<div style=\"background: #f4f4f4; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                    "  <p style=\"margin-top:0;\"><strong>Share this on social media:</strong></p>" +
                    "  <p style=\"font-style: italic;\">\"So excited to be featured as a top artist on @MiksterMedia this week! Check out my profile and latest AI tracks: " + artistUrl + "\"</p>" +
                    "  <div style=\"margin-top: 15px;\">" +
                    "    <a href=\"" + twitterUrl + "\" style=\"display:inline-block; background:#1DA1F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px; margin-right:10px;\">Share on X (Twitter)</a>" +
                    "    <a href=\"" + facebookUrl + "\" style=\"display:inline-block; background:#1877F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px;\">Share on Facebook</a>" +
                    "  </div>" +
                    "</div>" +
                    "<p>Keep up the great work!</p>" +
                    "<p>— The Mikster Media Team</p>";

            sendResendEmail(email, "Congratulations! You are a featured artist on Mikster Media!", htmlContent);
            log.info("Featured artist email sent to {} via Resend API", email);

        } catch (Exception e) {
            log.error("Failed to send featured artist email to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void sendExtendedFeaturedArtistEmail(String email, String artistName, Long artistId) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("RESEND_API_KEY is not set. Skipping extended featured artist email to {}", email);
            return;
        }

        try {
            String artistUrl = "https://mikstermedia.com/artists"; 
            String shareText = java.net.URLEncoder.encode("My artist profile is blowing up and just got its feature extended on the front page of @MiksterMedia! Let's keep it going, check out my tracks here: " + artistUrl, "UTF-8");
            String twitterUrl = "https://twitter.com/intent/tweet?text=" + shareText;
            String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=" + java.net.URLEncoder.encode(artistUrl, "UTF-8") + "&quote=" + shareText;

            String htmlContent = "<h2>Hi " + artistName + ",</h2>" +
                    "<p>Your profile has been performing incredibly well with the community! Because of its popularity, we have decided to <strong>extend your feature</strong> on the front page of Mikster Media.</p>" +
                    "<p>Keep the momentum going! We’d love for you to share the good news with your fans so they can continue to follow your work.</p>" +
                    "<div style=\"background: #f4f4f4; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                    "  <p style=\"margin-top:0;\"><strong>Share this on social media:</strong></p>" +
                    "  <p style=\"font-style: italic;\">\"My artist profile is blowing up and just got its feature extended on the front page of @MiksterMedia! Let's keep it going, check out my tracks here: " + artistUrl + "\"</p>" +
                    "  <div style=\"margin-top: 15px;\">" +
                    "    <a href=\"" + twitterUrl + "\" style=\"display:inline-block; background:#1DA1F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px; margin-right:10px;\">Share on X (Twitter)</a>" +
                    "    <a href=\"" + facebookUrl + "\" style=\"display:inline-block; background:#1877F2; color:white; padding:10px 15px; text-decoration:none; border-radius:5px;\">Share on Facebook</a>" +
                    "  </div>" +
                    "</div>" +
                    "<p>Congratulations again!</p>" +
                    "<p>— The Mikster Media Team</p>";

            sendResendEmail(email, "Your profile is on fire! We've extended your feature on Mikster Media 🔥", htmlContent);
            log.info("Extended featured artist email sent to {} via Resend API", email);

        } catch (Exception e) {
            log.error("Failed to send extended featured artist email to {}: {}", email, e.getMessage());
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

    @Async
    public void sendCustomEmailBlast(List<String> emails, String subject, String body) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("RESEND_API_KEY is not set. Skipping bulk email.");
            return;
        }
        
        String htmlContent = "<div style=\"font-family: sans-serif; max-width: 600px; margin: 0 auto;\">" +
                "<p style=\"white-space: pre-wrap;\">" + body + "</p>" +
                "<br>" +
                "<p style=\"color: #666; font-size: 0.9em;\">You are receiving this because you are a member of Mikster Media.</p>" +
                "</div>";
                
        for (String email : emails) {
            try {
                sendResendEmail(email, subject, htmlContent);
                log.info("Bulk email sent to {} via Resend API", email);
            } catch (Exception e) {
                log.error("Failed to send bulk email to {}: {}", email, e.getMessage());
            }
        }
    }

    private void sendResendEmail(String to, String subject, String htmlContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        String formattedFrom = fromEmail;
        if (!formattedFrom.contains("<")) {
            formattedFrom = "Mikster Media <" + formattedFrom + ">";
        }

        Map<String, Object> payload = Map.of(
                "from", formattedFrom,
                "to", List.of(to),
                "subject", subject,
                "html", htmlContent
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.exchange("https://api.resend.com/emails", HttpMethod.POST, request, String.class);
    }
}
