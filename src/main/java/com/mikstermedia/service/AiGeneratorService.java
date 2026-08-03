package com.mikstermedia.service;

import com.mikstermedia.model.AiGenerator;
import com.mikstermedia.repository.AiGeneratorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.List;

/**
 * Service managing AI Music Generator tools, seeding default tools,
 * and verifying URL reachability on a daily schedule.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AiGeneratorService {

    private final AiGeneratorRepository aiGeneratorRepository;

    @Transactional(readOnly = true)
    public List<AiGenerator> getAllGenerators() {
        return aiGeneratorRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public AiGenerator getGeneratorById(Long id) {
        return aiGeneratorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI Generator not found with id: " + id));
    }

    public AiGenerator saveGenerator(AiGenerator generator) {
        if (generator.getDisplayOrder() == null) {
            generator.setDisplayOrder((int) (aiGeneratorRepository.count() + 1));
        }
        return aiGeneratorRepository.save(generator);
    }

    public void deleteGenerator(Long id) {
        aiGeneratorRepository.deleteById(id);
    }

    /**
     * Runs once daily at 6:00 AM UTC to verify that each platform's website URL is alive and reachable.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void verifyAllGeneratorUrlsScheduled() {
        log.info("Starting daily verification of AI Generator URLs...");
        verifyAllGeneratorUrls();
        log.info("Finished daily verification of AI Generator URLs.");
    }

    public void verifyAllGeneratorUrls() {
        List<AiGenerator> generators = aiGeneratorRepository.findAll();
        Instant now = Instant.now();
        for (AiGenerator gen : generators) {
            boolean reachable = checkUrlReachable(gen.getWebsiteUrl());
            gen.setUrlReachable(reachable);
            gen.setLastVerifiedAt(now);
            aiGeneratorRepository.save(gen);
            log.info("Verified AI Generator [{}]: reachable={}", gen.getName(), reachable);
        }
    }

    private boolean checkUrlReachable(String targetUrl) {
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (MiksterMedia-Bot/1.0)");
            int status = connection.getResponseCode();
            return status >= 200 && status < 400;
        } catch (Exception e) {
            log.warn("URL unreachable for [{}]: {}", targetUrl, e.getMessage());
            return false;
        }
    }

    /**
     * Seed default AI Generators if table is empty.
     */
    public void seedDefaultGeneratorsIfEmpty() {
        if (aiGeneratorRepository.count() > 0) {
            return;
        }
        log.info("Seeding default AI Generators for Comparison Hub...");

        aiGeneratorRepository.save(AiGenerator.builder()
                .slug("suno")
                .name("Suno AI (v4.0)")
                .logo("☀️")
                .tagline("The industry benchmark for full songs with realistic vocals and song structure from any prompt.")
                .category("vocal")
                .websiteUrl("https://suno.com")
                .rating(4.9)
                .verdict("The leader for vocal pop, hip-hop, rock, and lyric-driven songs. Unmatched realism in vocal delivery and song sections.")
                .freeTier("50 credits/day (~10 songs). Non-commercial use. Basic MP3 audio.")
                .proTier("$10/mo — 2,500 credits/mo (~500 songs). Full commercial rights, general priority generation.")
                .premierTier("$30/mo — 10,000 credits/mo (~2,000 songs). Commercial rights, fast queue, stems separation.")
                .maxTrackLength("4 mins (extendable)")
                .hasStems(true)
                .hasInpainting(true)
                .hasCustomLyrics(true)
                .hasMidiExport(false)
                .commercialRightsOnPaid(true)
                .audioQuality("MP3 / WAV (Paid)")
                .bestForTags("Vocals & Pop,Full Songs,Songwriting Demo,All Genres")
                .displayOrder(1)
                .urlReachable(true)
                .lastVerifiedAt(Instant.now())
                .build());

        aiGeneratorRepository.save(AiGenerator.builder()
                .slug("udio")
                .name("Udio (v1.5)")
                .logo("🎧")
                .tagline("High-fidelity AI music generation with advanced inpainting, stem control, and granular track extension.")
                .category("vocal")
                .websiteUrl("https://udio.com")
                .rating(4.8)
                .verdict("Highest acoustic clarity and instrumental separation. Essential for producers who want to extend and fine-tune specific intro/outro sections.")
                .freeTier("10 credits/day. Non-commercial use. Standard quality streams.")
                .proTier("$10/mo — 1,200 credits/mo. Full commercial rights, inpainting & extension, WAV downloads.")
                .premierTier("$30/mo — 4,800 credits/mo. Commercial rights, priority queue, stem control.")
                .maxTrackLength("15 mins (via extend)")
                .hasStems(true)
                .hasInpainting(true)
                .hasCustomLyrics(true)
                .hasMidiExport(false)
                .commercialRightsOnPaid(true)
                .audioQuality("Lossless WAV")
                .bestForTags("Audiophile Clarity,EDM & Electronic,Granular Inpainting,Complex Mixes")
                .displayOrder(2)
                .urlReachable(true)
                .lastVerifiedAt(Instant.now())
                .build());

        aiGeneratorRepository.save(AiGenerator.builder()
                .slug("stable-audio")
                .name("Stable Audio 2.0")
                .logo("🌊")
                .tagline("Generate full-length musical tracks up to 3 minutes with structure, coherence, and sound effects.")
                .category("instrumental")
                .websiteUrl("https://stableaudio.com")
                .rating(4.5)
                .verdict("Excellent for producers and beatmakers needing clean instrumental samples, loops, and cinematic beds without copyright worries.")
                .freeTier("20 generations/month. Non-commercial use. 44.1kHz MP3.")
                .proTier("$11.99/mo — 500 generations/month. Full commercial rights, high-quality WAV downloads.")
                .premierTier("Custom Enterprise pricing — unlimited team access, custom model fine-tuning.")
                .maxTrackLength("3 mins")
                .hasStems(false)
                .hasInpainting(false)
                .hasCustomLyrics(false)
                .hasMidiExport(false)
                .commercialRightsOnPaid(true)
                .audioQuality("44.1kHz Stereo WAV")
                .bestForTags("Cinematic Beds,Background Loops,DAW Sample Packs,Sound Effects")
                .displayOrder(3)
                .urlReachable(true)
                .lastVerifiedAt(Instant.now())
                .build());

        aiGeneratorRepository.save(AiGenerator.builder()
                .slug("soundraw")
                .name("Soundraw")
                .logo("🎛️")
                .tagline("Royalty-free AI music generator tailored for video creators, editors, and content producers.")
                .category("instrumental")
                .websiteUrl("https://soundraw.io")
                .rating(4.6)
                .verdict("The most practical tool for video creators who need to lengthen, shorten, or drop instruments at specific timestamps.")
                .freeTier("Unlimited generation & bookmarking. No downloads.")
                .proTier("$16.99/mo — Creator Tier. Commercial rights for YouTube/social videos, unlimited downloads.")
                .premierTier("$29.99/mo — Artist Tier. Distribution to Spotify/Apple Music, stems download, monetization.")
                .maxTrackLength("5 mins (custom blocks)")
                .hasStems(true)
                .hasInpainting(true)
                .hasCustomLyrics(false)
                .hasMidiExport(false)
                .commercialRightsOnPaid(true)
                .audioQuality("16-bit 44.1kHz WAV")
                .bestForTags("Video Editors,Custom Intro/Outro,Royalty-Free Beats,Content Creators")
                .displayOrder(4)
                .urlReachable(true)
                .lastVerifiedAt(Instant.now())
                .build());

        aiGeneratorRepository.save(AiGenerator.builder()
                .slug("mubert")
                .name("Mubert")
                .logo("🔊")
                .tagline("Real-time generative music streaming and custom-length soundtracks for apps, streams, and videos.")
                .category("instrumental")
                .websiteUrl("https://mubert.com")
                .rating(4.4)
                .verdict("Fantastic for instant electronic and lofi background soundtracks of exact custom lengths up to 25 minutes.")
                .freeTier("Ambient streams, MP3 downloads with Mubert audio watermark.")
                .proTier("$14/mo — 500 tracks/mo. YouTube/Twitch commercial use, no watermark.")
                .premierTier("$39/mo — Pro Tier. Commercial rights for client work, games, and apps, WAV downloads.")
                .maxTrackLength("Up to 25 mins")
                .hasStems(false)
                .hasInpainting(false)
                .hasCustomLyrics(false)
                .hasMidiExport(false)
                .commercialRightsOnPaid(true)
                .audioQuality("WAV & MP3")
                .bestForTags("Live Streams,Gaming Background,Lofi & Electronic,Exact Timestamps")
                .displayOrder(5)
                .urlReachable(true)
                .lastVerifiedAt(Instant.now())
                .build());

        aiGeneratorRepository.save(AiGenerator.builder()
                .slug("aiva")
                .name("AIVA")
                .logo("🎻")
                .tagline("The AI virtual artist composing emotional orchestral, cinematic, and hybrid scores with MIDI export.")
                .category("instrumental")
                .websiteUrl("https://aiva.ai")
                .rating(4.7)
                .verdict("Essential for composers and producers who want MIDI export to assign their own VST instruments in Logic, Ableton, or Kontakt.")
                .freeTier("3 downloads/month. Non-commercial use. AIVA owns copyright.")
                .proTier("€11/mo — 15 downloads/month. YouTube/social monetization, AIVA owns copyright.")
                .premierTier("€33/mo — Pro Tier. You own full copyright & monetization rights. MIDI + WAV + Stems.")
                .maxTrackLength("5+ mins")
                .hasStems(true)
                .hasInpainting(true)
                .hasCustomLyrics(false)
                .hasMidiExport(true)
                .commercialRightsOnPaid(true)
                .audioQuality("WAV + MIDI + Stems")
                .bestForTags("Orchestral Scores,Film & Game Music,MIDI Export,DAW Composers")
                .displayOrder(6)
                .urlReachable(true)
                .lastVerifiedAt(Instant.now())
                .build());

        log.info("Successfully seeded 6 AI Generators.");
    }
}
