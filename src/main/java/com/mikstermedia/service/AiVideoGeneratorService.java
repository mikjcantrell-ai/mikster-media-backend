package com.mikstermedia.service;

import com.mikstermedia.model.AiVideoGenerator;
import com.mikstermedia.repository.AiVideoGeneratorRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiVideoGeneratorService {

    private final AiVideoGeneratorRepository repository;

    public void seedDefaultGeneratorsIfEmpty() {
        if (repository.count() == 0) {
            log.info("Seeding initial AI Video & Visualizer Generators for Video Comparison Hub...");

            repository.save(AiVideoGenerator.builder()
                    .slug("muvio")
                    .name("Muvio")
                    .logo("🎬")
                    .tagline("Turn your AI songs into stunning, beat-synced music videos and audio visualizers in minutes.")
                    .category("music_video")
                    .websiteUrl("https://muvio.ai")
                    .rating(4.9)
                    .verdict("The #1 choice for AI musicians creating full-song music videos for YouTube, TikTok, and Instagram Reels.")
                    .freeTier("Free preview renders (720p watermarked)")
                    .proTier("$12/mo - Unwatermarked 1080p HD & 4K exports, priority GPU rendering")
                    .premierTier("$29/mo - Unlimited 4K cinematic exports, custom lyric visualizers, commercial rights")
                    .maxResolution("4K UHD (60fps)")
                    .maxVideoLength("Full Song (5+ min)")
                    .hasBeatSync(true)
                    .hasAudioReactive(true)
                    .hasSingingAvatar(false)
                    .hasStemSync(false)
                    .commercialRightsOnPaid(true)
                    .bestForTags("Music Videos,Visualizers,Beat-Sync")
                    .displayOrder(1)
                    .urlReachable(true)
                    .lastVerifiedAt(Instant.now())
                    .build());

            repository.save(AiVideoGenerator.builder()
                    .slug("kaiber")
                    .name("Kaiber")
                    .logo("⚡")
                    .tagline("The industry-standard AI video engine for audio-reactive animations. Watch your visuals pulse, morph, and evolve to the rhythm and stems of your song.")
                    .category("visualizer")
                    .websiteUrl("https://kaiber.ai")
                    .rating(4.8)
                    .verdict("Best-in-class audio-reactive animation engine with multi-stem reactivity and custom style morphing.")
                    .freeTier("7-day trial with 30 free credits (watermarked)")
                    .proTier("$15/mo - 1,000 monthly credits, 1080p HD, unwatermarked commercial use")
                    .premierTier("$30/mo - 2,500 credits, 4K upscale, custom seed frames, camera motion controls")
                    .maxResolution("4K UHD")
                    .maxVideoLength("Full Song (5+ min)")
                    .hasBeatSync(true)
                    .hasAudioReactive(true)
                    .hasSingingAvatar(false)
                    .hasStemSync(true)
                    .commercialRightsOnPaid(true)
                    .bestForTags("Audio-Reactive,Animation,4K Video")
                    .displayOrder(2)
                    .urlReachable(true)
                    .lastVerifiedAt(Instant.now())
                    .build());

            repository.save(AiVideoGenerator.builder()
                    .slug("noisee")
                    .name("Noisee")
                    .logo("📱")
                    .tagline("Paste a Suno or Udio link and generate viral, beat-synchronized short-form music videos for YouTube Shorts and TikTok in under 60 seconds.")
                    .category("short_form")
                    .websiteUrl("https://noisee.ai")
                    .rating(4.7)
                    .verdict("The fastest way to turn Suno & Udio URLs into engaging vertical videos for TikTok and YouTube Shorts.")
                    .freeTier("3 free video clips per day (watermarked)")
                    .proTier("$10/mo - Unlimited short-form videos, unwatermarked vertical 1080p")
                    .premierTier("$25/mo - Full track stitching, custom aspect ratios, priority Discord bot access")
                    .maxResolution("1080p Vertical / HD")
                    .maxVideoLength("Up to 90 Sec")
                    .hasBeatSync(true)
                    .hasAudioReactive(true)
                    .hasSingingAvatar(false)
                    .hasStemSync(false)
                    .commercialRightsOnPaid(true)
                    .bestForTags("Suno / Udio,Shorts,Viral")
                    .displayOrder(3)
                    .urlReachable(true)
                    .lastVerifiedAt(Instant.now())
                    .build());

            repository.save(AiVideoGenerator.builder()
                    .slug("openart")
                    .name("OpenArt")
                    .logo("🎨")
                    .tagline("Create custom album artwork, character designs, and cinematic storyboard concepts for your AI music releases using advanced AI image models.")
                    .category("music_video")
                    .websiteUrl("https://openart.ai")
                    .rating(4.8)
                    .verdict("Essential AI art suite for generating album artwork and storyboard frames for music visualizers.")
                    .freeTier("50 free starter credits upon signup")
                    .proTier("$12/mo - 5,000 credits, custom LoRA model training, commercial license")
                    .premierTier("$28/mo - Unlimited relax mode, priority 4K generations, Flux & SDXL models")
                    .maxResolution("4K High Resolution")
                    .maxVideoLength("Storyboard / Stills")
                    .hasBeatSync(false)
                    .hasAudioReactive(false)
                    .hasSingingAvatar(false)
                    .hasStemSync(false)
                    .commercialRightsOnPaid(true)
                    .bestForTags("Album Cover,AI Art,Storyboards")
                    .displayOrder(4)
                    .urlReachable(true)
                    .lastVerifiedAt(Instant.now())
                    .build());

            repository.save(AiVideoGenerator.builder()
                    .slug("hedra")
                    .name("Hedra")
                    .logo("🎤")
                    .tagline("Create expressive animated characters and artist personas that sing your lyrics with lifelike facial animation and lip-sync accuracy.")
                    .category("avatar")
                    .websiteUrl("https://hedra.com")
                    .rating(4.7)
                    .verdict("Top platform for AI virtual artists and lip-sync character performances.")
                    .freeTier("Free 30-second character generations (watermarked)")
                    .proTier("$14/mo - 1080p HD character animations, 10 minutes monthly generation")
                    .premierTier("$35/mo - 4K character export, custom voice clone integration, priority lip-sync engine")
                    .maxResolution("1080p HD / 4K")
                    .maxVideoLength("Up to 3 mins")
                    .hasBeatSync(true)
                    .hasAudioReactive(false)
                    .hasSingingAvatar(true)
                    .hasStemSync(false)
                    .commercialRightsOnPaid(true)
                    .bestForTags("Lip-Sync,Singing Avatar,Character")
                    .displayOrder(5)
                    .urlReachable(true)
                    .lastVerifiedAt(Instant.now())
                    .build());

            repository.save(AiVideoGenerator.builder()
                    .slug("lalal-ai")
                    .name("LALAL.AI")
                    .logo("🎛️")
                    .tagline("High-precision AI stem splitting. Extract vocals, drums, bass, and synths from any AI audio track for remixing, mastering, and video syncing.")
                    .category("visualizer")
                    .websiteUrl("https://www.lalal.ai")
                    .rating(4.9)
                    .verdict("The gold standard for stem separation to power multi-stem audio-reactive video animations.")
                    .freeTier("Free preview splitting on up to 10 minutes of audio")
                    .proTier("$15 one-time - 90 minutes of audio processing, WAV/FLAC lossless export")
                    .premierTier("$30 one-time - 300 minutes, stem batch processing, API integration")
                    .maxResolution("Lossless Audio / WAV")
                    .maxVideoLength("Unlimited (Full Track)")
                    .hasBeatSync(true)
                    .hasAudioReactive(false)
                    .hasSingingAvatar(false)
                    .hasStemSync(true)
                    .commercialRightsOnPaid(true)
                    .bestForTags("Stem Splitter,Vocals,Mastering")
                    .displayOrder(6)
                    .urlReachable(true)
                    .lastVerifiedAt(Instant.now())
                    .build());

            repository.save(AiVideoGenerator.builder()
                    .slug("plazma")
                    .name("Plazma Visuals")
                    .logo("🌈")
                    .tagline("Customizable 3D audio-visualizers and spectrogram templates designed specifically for electronic, synthwave, and EDM AI music.")
                    .category("visualizer")
                    .websiteUrl("https://plazma.app")
                    .rating(4.6)
                    .verdict("Outstanding real-time 3D spectrograms and neon visualizers for synthwave and electronic tracks.")
                    .freeTier("Free 720p exports with Plazma watermark")
                    .proTier("$9/mo - 1080p & 4K 60fps exports, custom logo overlays, no watermark")
                    .premierTier("$19/mo - 3D scene customization, multi-camera presets, commercial broadcast license")
                    .maxResolution("4K UHD 60fps")
                    .maxVideoLength("Full Song (10+ min)")
                    .hasBeatSync(true)
                    .hasAudioReactive(true)
                    .hasSingingAvatar(false)
                    .hasStemSync(true)
                    .commercialRightsOnPaid(true)
                    .bestForTags("3D Spectrogram,Synthwave,Real-time")
                    .displayOrder(7)
                    .urlReachable(true)
                    .lastVerifiedAt(Instant.now())
                    .build());

            repository.save(AiVideoGenerator.builder()
                    .slug("rotor")
                    .name("Rotor Videos")
                    .logo("🎥")
                    .tagline("Upload your AI audio and select style clips. Rotor automatically edits and syncs stock footage and effects to your song's beats and drops.")
                    .category("music_video")
                    .websiteUrl("https://rotorvideos.com")
                    .rating(4.7)
                    .verdict("The easiest way to produce professional live-action and VFX music videos synced to AI song beats.")
                    .freeTier("Create and preview full videos for free")
                    .proTier("$9 per video download - 1080p HD broadcast quality")
                    .premierTier("$19 per video - 4K UHD download, full commercial royalty-free footage license")
                    .maxResolution("4K UHD Cinematic")
                    .maxVideoLength("Full Song (Unlimited)")
                    .hasBeatSync(true)
                    .hasAudioReactive(false)
                    .hasSingingAvatar(false)
                    .hasStemSync(false)
                    .commercialRightsOnPaid(true)
                    .bestForTags("Auto-Edit,Beat Detection,Stock Library")
                    .displayOrder(8)
                    .urlReachable(true)
                    .lastVerifiedAt(Instant.now())
                    .build());

            log.info("Successfully seeded 8 AI Video generators into database.");
        }
    }

    public List<AiVideoGenerator> getAllGenerators() {
        return repository.findAllByOrderByDisplayOrderAsc();
    }

    public AiVideoGenerator getGeneratorById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Video generator not found: " + id));
    }

    public AiVideoGenerator saveGenerator(AiVideoGenerator generator) {
        return repository.save(generator);
    }

    public void deleteGenerator(Long id) {
        repository.deleteById(id);
    }

    public void verifyAllGeneratorUrls() {
        List<AiVideoGenerator> generators = repository.findAll();
        for (AiVideoGenerator gen : generators) {
            boolean reachable = isUrlReachable(gen.getWebsiteUrl());
            gen.setUrlReachable(reachable);
            gen.setLastVerifiedAt(Instant.now());
            repository.save(gen);
            log.info("Verified video URL: {} -> {}", gen.getWebsiteUrl(), reachable);
        }
    }

    private boolean isUrlReachable(String urlStr) {
        try {
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (MM:AI Status Bot)");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (Exception e) {
            log.warn("Video generator URL {} unreachable: {}", urlStr, e.getMessage());
            return false;
        }
    }
}
