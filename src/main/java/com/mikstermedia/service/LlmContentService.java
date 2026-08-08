package com.mikstermedia.service;

import com.mikstermedia.model.Artist;
import com.mikstermedia.model.Track;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LlmContentService {

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={key}";

    private final RestClient restClient;

    public LlmContentService() {
        this.restClient = RestClient.create();
    }

    /**
     * Generates a 150-word track review.
     */
    public String generateTrackReview(Track track) {
        if (!isConfigured()) return null;

        String prompt = String.format(
            "Write a 150-word engaging and descriptive music review for the AI-generated track '%s' by '%s'. " +
            "The genre is %s. Describe the vibe of the song and how AI is shaping this sound. " +
            "Do not use markdown, just write plain paragraphs.",
            track.getTitle(), track.getCreator(), track.getGenre() != null ? track.getGenre() : "unknown"
        );

        return callGemini(prompt);
    }

    /**
     * Generates a 200-word artist biography.
     */
    public String generateArtistBio(Artist artist) {
        if (!isConfigured()) return null;

        String prompt = String.format(
            "Write a 200-word creative biography for an AI music artist named '%s'. " +
            "Their primary genre is %s, and they use AI tools like %s. " +
            "Focus on their innovative sound and musical journey. Do not use markdown.",
            artist.getName(), 
            artist.getPrimaryGenre() != null ? artist.getPrimaryGenre() : "experimental",
            artist.getAiToolsUsed() != null ? artist.getAiToolsUsed() : "various AI models"
        );

        return callGemini(prompt);
    }

    /**
     * Generates a 500-word blog post rounding up top tracks.
     */
    public String generateWeeklyRoundup(List<Track> topTracks) {
        if (!isConfigured() || topTracks.isEmpty()) return null;

        StringBuilder tracksInfo = new StringBuilder();
        for (int i = 0; i < topTracks.size(); i++) {
            Track t = topTracks.get(i);
            tracksInfo.append(String.format("%d. '%s' by %s (%s)\n", i + 1, t.getTitle(), t.getCreator(), t.getGenre()));
        }

        String prompt = String.format(
            "You are a music journalist writing for an AI music discovery platform. " +
            "Write a 500-word blog post titled 'Top AI Tracks of the Week'. " +
            "Discuss the following tracks and why they are making waves in the AI music space:\n%s\n" +
            "Use engaging HTML paragraphs (<p>) and headers (<h3>) so it's ready to be rendered on a website.",
            tracksInfo.toString()
        );

        return callGemini(prompt);
    }

    private boolean isConfigured() {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("Gemini API key is not configured (gemini.api-key). Cannot generate content.");
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private String callGemini(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                )
            );

            Map<?, ?> response = restClient.post()
                .uri(GEMINI_URL, geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

            if (response != null && response.containsKey("candidates")) {
                List<?> candidates = (List<?>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
                    Map<?, ?> content = (Map<?, ?>) candidate.get("content");
                    List<?> parts = (List<?>) content.get("parts");
                    if (!parts.isEmpty()) {
                        Map<?, ?> part = (Map<?, ?>) parts.get(0);
                        return (String) part.get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate content with Gemini: {}", e.getMessage());
        }
        return null;
    }
}
