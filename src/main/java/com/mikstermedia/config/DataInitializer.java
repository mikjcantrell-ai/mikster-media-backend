package com.mikstermedia.config;

import com.mikstermedia.model.*;
import com.mikstermedia.repository.*;
import com.mikstermedia.service.WeeklyChartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with sample tracks, chart entries, and platform settings
 * on first startup — only if the tables are empty.
 *
 * <p>This component runs after the Spring context is fully loaded (via
 * {@link CommandLineRunner}), guaranteeing Hibernate has created all tables.
 *
 * <p>Seed data covers all three supported platforms (Spotify, YouTube, Apple)
 * so the frontend embed components have real data to render immediately.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TrackRepository trackRepository;
    private final WeeklyChartRepository weeklyChartRepository;
    private final PlatformSettingRepository settingRepository;
    private final WeeklyChartService weeklyChartService;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;
    private final NewReleaseRepository newReleaseRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedPlatformSettings();
        seedTracks();
        seedChartEntries();
        seedArtists();
        seedGenres();
        seedNewReleases();
        log.info("✅ Data initialization complete.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN USER (seeded from application.properties)
    // ─────────────────────────────────────────────────────────────────────────

    private void seedAdminUser() {
        // If the admin username already exists in DB, skip (idempotent)
        if (userRepository.findByUsernameIgnoreCase(adminUsername).isPresent()) return;
        log.info("Seeding initial admin user: {}", adminUsername);

        AppUser admin = new AppUser();
        admin.setUsername(adminUsername);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setEmail("admin@aimusicweb.io");
        admin.setRole("ADMIN");
        admin.setDisplayName("Platform Admin");
        admin.setActive(true);
        userRepository.save(admin);
        log.info("Admin user '{}' created in app_users table.", adminUsername);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PLATFORM SETTINGS
    // ─────────────────────────────────────────────────────────────────────────

    private void seedPlatformSettings() {
        // Full seed is skipped if any settings already exist
        if (settingRepository.count() > 0) {
            // But always ensure the featured-duration keys exist (added after initial seed)
            ensureSettingExists("featured_artist_days", "14", "Days an artist stays featured before auto-expiring");
            ensureSettingExists("featured_track_days", "14", "Days a track stays featured before auto-expiring");
            ensureSettingExists("GENRE_FEATURED_LIMIT", "5", "Maximum number of genres that can be featured at once");
            ensureSettingExists("GENRE_FEATURED_DAYS", "30", "Days a genre stays featured before auto-expiring");
            ensureSettingExists("security_access_submit", "CREATOR,PRODUCER", "Tiers allowed to submit tracks");
            ensureSettingExists("security_access_collab", "CREATOR,PRODUCER", "Tiers allowed to access collab board");
            ensureSettingExists("security_access_guides", "CREATOR,PRODUCER", "Tiers allowed to access creator guides");
            return;
        }
        log.info("Seeding platform settings...");

        if (settingRepository.count() == 0) {
            settingRepository.save(new PlatformSetting("site_title", "AI Music Web"));
            settingRepository.save(new PlatformSetting("featured_genre", "Electronic"));
            settingRepository.save(new PlatformSetting("chart_refresh_hour", "0"));
            settingRepository.save(new PlatformSetting("security_access_submit", "CREATOR,PRODUCER"));
            settingRepository.save(new PlatformSetting("security_access_collab", "CREATOR,PRODUCER"));
            settingRepository.save(new PlatformSetting("security_access_guides", "CREATOR,PRODUCER"));
        }
        settingRepository.save(new PlatformSetting("site_tagline", "The Future of Music is Generated"));
        settingRepository.save(new PlatformSetting("contact_email", "hello@aimusicweb.io"));
        settingRepository.save(new PlatformSetting("about_mission",
            "We champion AI music creators worldwide — spotlighting generative art built with Suno, Udio, Stable Audio, and beyond."));
        // How long artists / tracks stay in the Featured section (in days)
        settingRepository.save(new PlatformSetting("featured_artist_days", "14"));
        settingRepository.save(new PlatformSetting("featured_track_days",  "14"));
    }

    private void ensureSettingExists(String key, String defaultValue, String description) {
        settingRepository.findById(key).orElseGet(() -> {
            PlatformSetting s = new PlatformSetting(key, defaultValue);
            // If the entity had a description field, we'd set it here.
            return settingRepository.save(s);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRACKS
    // ─────────────────────────────────────────────────────────────────────────

    private Track createTrack(String title, String creator, String mediaUrl, String platformSource, String aiToolsUsed, String genre, boolean featuredStatus, int displayOrder, String embedUrl, String videoUrl) {
        Track t = new Track();
        t.setTitle(title);
        t.setCreator(creator);
        t.setMediaUrl(mediaUrl);
        t.setPlatformSource(platformSource);
        t.setAiToolsUsed(aiToolsUsed);
        t.setGenre(genre);
        t.setFeaturedStatus(featuredStatus);
        t.setDisplayOrder(displayOrder);
        t.setEmbedUrl(embedUrl);
        t.setVideoUrl(videoUrl);
        return t;
    }

    private void seedTracks() {
        if (trackRepository.count() > 0) return;
        log.info("Seeding sample tracks...");

        // ── Spotify tracks ────────────────────────────────────────────────────
        trackRepository.save(createTrack(
            "Neon Dreamscape", "SynthWave_AI",
            "https://open.spotify.com/track/6rqhFgbbKwnb9MLmUQDhG6",
            "Spotify", "Suno v3, Udio", "Electronic", true, 0,
            "https://open.spotify.com/embed/track/6rqhFgbbKwnb9MLmUQDhG6",
            null
        ));

        trackRepository.save(createTrack(
            "Digital Solitude", "AmbientCore",
            "https://open.spotify.com/track/4iV5W9uYEdYUVa79Axb7Rh",
            "Spotify", "Suno v3", "Ambient", true, 1,
            "https://open.spotify.com/embed/track/4iV5W9uYEdYUVa79Axb7Rh",
            null
        ));

        trackRepository.save(createTrack(
            "Algorithm Blues", "Neural_Jazz",
            "https://open.spotify.com/track/1301WleyT98MSxVHPZCA6M",
            "Spotify", "Udio, MusicGen", "Jazz", false, 0,
            "https://open.spotify.com/embed/track/1301WleyT98MSxVHPZCA6M",
            null
        ));

        // ── YouTube tracks ────────────────────────────────────────────────────
        trackRepository.save(createTrack(
            "Synthetic Sunrise", "DawnAI",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "YouTube", "Suno v3, Midjourney (visuals)", "Lo-Fi", true, 2,
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        ));

        trackRepository.save(createTrack(
            "Quantum Pulse", "BeatForge_AI",
            "https://www.youtube.com/watch?v=9bZkp7q19f0",
            "YouTube", "Udio, Stable Diffusion (video)", "EDM", false, 0,
            "https://www.youtube.com/embed/9bZkp7q19f0",
            "https://www.youtube.com/watch?v=9bZkp7q19f0"
        ));

        trackRepository.save(createTrack(
            "Ghost Protocol", "CipherSound",
            "https://www.youtube.com/watch?v=kJQP7kiw5Fk",
            "YouTube", "MusicGen, AudioLDM", "Darkwave", false, 0,
            "https://www.youtube.com/embed/kJQP7kiw5Fk",
            "https://www.youtube.com/watch?v=kJQP7kiw5Fk"
        ));

        // ── Apple Music tracks ────────────────────────────────────────────────
        trackRepository.save(createTrack(
            "Chrome Garden", "MetalMind_AI",
            "https://music.apple.com/us/album/chrome-garden/1700000001?i=1700000001",
            "Apple", "Suno v3, AudioGen", "Indie Pop", true, 3,
            null, null
        ));

        trackRepository.save(createTrack(
            "Binary Bloom", "FloralAI",
            "https://music.apple.com/us/album/binary-bloom/1700000002?i=1700000002",
            "Apple", "Udio, Riffusion", "Dream Pop", false, 0,
            null, null
        ));

        trackRepository.save(createTrack(
            "The Last Transistor", "RetroFuture_AI",
            "https://music.apple.com/us/album/last-transistor/1700000003?i=1700000003",
            "Apple", "Suno v3, MusicGen", "Synthpop", false, 0,
            null, null
        ));

        trackRepository.save(createTrack(
            "Hyperspace Hymn", "CosmicAI",
            "https://www.youtube.com/watch?v=L_jWHffIx5E",
            "YouTube", "Suno v4, Udio, Stable Audio", "Space Ambient", true, 4,
            "https://www.youtube.com/embed/L_jWHffIx5E",
            "https://www.youtube.com/watch?v=L_jWHffIx5E"
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WEEKLY CHART ENTRIES
    // ─────────────────────────────────────────────────────────────────────────

    private void seedChartEntries() {
        if (weeklyChartRepository.count() > 0) return;
        log.info("Seeding weekly chart entries...");

        // Fetch all tracks — we'll place the first 10 on the chart
        var tracks = trackRepository.findAll();
        if (tracks.isEmpty()) return;

        // Seed play and upvote counts so the algorithm has data to work with
        int[][] seedMetrics = {
            {380, 95},  // weeklyPlays, upvoteCount for track[0]
            {310, 82},
            {290, 75},
            {265, 61},
            {240, 58},
            {215, 49},
            {190, 44},
            {165, 38},
            {140, 31},
            {110, 22}
        };

        int max = Math.min(tracks.size(), 10);
        for (int i = 0; i < max; i++) {
            WeeklyChart entry = new WeeklyChart();
            entry.setTrack(tracks.get(i));
            entry.setWeeklyPlays(seedMetrics[i][0]);
            entry.setUpvoteCount(seedMetrics[i][1]);
            entry.setCurrentRank(i + 1);
            entry.setPreviousRank(i + 2 <= 10 ? i + 2 : null);
            entry.setRankChange(i == 0 ? "NEW" : (i % 3 == 0 ? "DOWN" : "UP"));
            weeklyChartRepository.save(entry);
        }

        // Run the ranking algorithm once to normalise all positions
        weeklyChartService.recalculateRankings();
        log.info("Chart seeded with {} entries and rankings recalculated.", max);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ARTISTS
    // ─────────────────────────────────────────────────────────────────────────

    private void seedArtists() {
        if (artistRepository.count() > 0) return;
        log.info("Seeding artists...");

        Artist a1 = new Artist(); a1.setName("SynthWave_AI"); a1.setBio("Pioneer of AI electronic music, blending algorithmic composition with human emotion. Known for hypnotic, layered synth textures."); a1.setCountry("US"); a1.setAiToolsUsed("Suno v3, Udio"); a1.setPrimaryGenre("Electronic"); a1.setFeaturedStatus(true); a1.setMonthlyListeners(42000); artistRepository.save(a1);
        Artist a2 = new Artist(); a2.setName("AmbientCore"); a2.setBio("Crafting soundscapes for the mind using pure AI generation. Specialises in meditative, evolving ambient textures."); a2.setCountry("UK"); a2.setAiToolsUsed("Suno v3"); a2.setPrimaryGenre("Ambient"); a2.setFeaturedStatus(true); a2.setMonthlyListeners(28500); artistRepository.save(a2);
        Artist a3 = new Artist(); a3.setName("Neural_Jazz"); a3.setBio("Merging jazz theory with AI improvisation to create genre-defying sonic experiences."); a3.setCountry("FR"); a3.setAiToolsUsed("Udio, MusicGen"); a3.setPrimaryGenre("Jazz"); a3.setFeaturedStatus(false); a3.setMonthlyListeners(15200); artistRepository.save(a3);
        Artist a4 = new Artist(); a4.setName("DawnAI"); a4.setBio("Lo-fi storytelling through generative music. Each track is a morning journal entry translated into sound."); a4.setCountry("JP"); a4.setAiToolsUsed("Suno v3, Midjourney"); a4.setPrimaryGenre("Lo-Fi"); a4.setFeaturedStatus(true); a4.setMonthlyListeners(61000); artistRepository.save(a4);
        Artist a5 = new Artist(); a5.setName("CosmicAI"); a5.setBio("Creating space ambient and electronic soundscapes that evoke the vast, mysterious universe."); a5.setCountry("DE"); a5.setAiToolsUsed("Suno v4, Udio, Stable Audio"); a5.setPrimaryGenre("Space Ambient"); a5.setFeaturedStatus(true); a5.setMonthlyListeners(37800); artistRepository.save(a5);
        Artist a6 = new Artist(); a6.setName("MetalMind_AI"); a6.setBio("Indie pop with an algorithmic twist. Writes hooks that feel human but are entirely machine-born."); a6.setCountry("AU"); a6.setAiToolsUsed("Suno v3, AudioGen"); a6.setPrimaryGenre("Indie Pop"); a6.setFeaturedStatus(false); a6.setMonthlyListeners(22100); artistRepository.save(a6);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // GENRES
    // ───────────────────────────────────────────────────────────────────────────

    private void seedGenres() {
        log.info("Seeding comprehensive genre list (additive)...");
        // Helper: only saves if the genre name doesn't already exist
        java.util.function.Consumer<Genre> save = g -> {
            if (!genreRepository.findByNameIgnoreCase(g.getName()).isPresent()) {
                genreRepository.save(g);
            }
        };

        // ── Core Popular Genres ────────────────────────────────────────────────
        save.accept(genre("Pop",              "Catchy, hook-driven AI pop with mainstream appeal and polished production.",           "#ff6b9d", "🎤", 0));
        save.accept(genre("Rock",             "Guitar-driven AI compositions spanning classic rock to modern alternative.",            "#e85d04", "🎸", 0));
        save.accept(genre("Hip-Hop",          "AI-generated beats, flows, and production spanning boom-bap to trap aesthetics.",      "#6a0dad", "🎤", 0));
        save.accept(genre("R&B",              "Smooth AI rhythms and soulful melodies rooted in classic rhythm and blues.",           "#c77dff", "🎵", 0));
        save.accept(genre("Country",          "AI-crafted storytelling with acoustic guitars, fiddles, and heartfelt lyrics.",        "#f4a261", "🤠", 0));
        save.accept(genre("Jazz",             "AI improvisations and compositions exploring harmony, swing, and improvisation.",      "#ffd700", "🎺", 1));
        save.accept(genre("Blues",            "Raw, emotive AI guitar and vocal compositions rooted in American blues tradition.",   "#1d3557", "🎸", 0));
        save.accept(genre("Soul",             "Deep, emotive AI music celebrating the tradition of classic soul and Motown.",         "#e63946", "❤️", 0));
        save.accept(genre("Classical",        "Orchestral and chamber AI compositions influenced by Western classical tradition.",    "#a8dadc", "🎻", 0));
        save.accept(genre("Folk",             "Acoustic storytelling AI music rooted in folk and Americana traditions.",             "#d4a373", "🪕", 0));

        // ── Electronic & Dance ─────────────────────────────────────────────────
        save.accept(genre("Electronic",       "AI-generated electronic music spanning synthwave, techno, and ambient electronica.",  "#00e5ff", "🎹", 3));
        save.accept(genre("EDM",              "High-energy dance music with AI-crafted drops, builds, and hooks.",                  "#39e27a", "🔥", 1));
        save.accept(genre("House",            "Four-to-the-floor AI house music with deep basslines and soulful samples.",          "#f77f00", "🏠", 0));
        save.accept(genre("Techno",           "Relentless, industrial AI techno built for the underground dance floor.",            "#023e8a", "⚙️", 0));
        save.accept(genre("Drum & Bass",      "High-tempo AI breakbeats and heavy bass lines defining the DnB sound.",             "#d00000", "🥁", 0));
        save.accept(genre("Dubstep",          "Bass-heavy AI music with signature wobble synths and half-time rhythms.",            "#240046", "🔊", 0));
        save.accept(genre("Trap",             "AI trap productions with rolling hi-hats, massive 808s, and dark atmospheres.",      "#370617", "🎤", 0));
        save.accept(genre("Synthwave",        "Retro-futuristic AI synthesizer music inspired by 80s film scores and new wave.",    "#7209b7", "🌆", 0));
        save.accept(genre("Ambient",          "Meditative, evolving AI soundscapes designed for focus, sleep, and creative flow.",  "#b06dff", "🌌", 2));
        save.accept(genre("Lo-Fi",            "Chill beats, warm textures, and nostalgic vibes — all generated by AI.",            "#ff4da6", "🎧", 1));
        save.accept(genre("Chillout",         "Downtempo AI music perfect for relaxation, yoga, and winding down.",                "#74b3ce", "😌", 0));

        // ── Indie & Alternative ────────────────────────────────────────────────
        save.accept(genre("Indie",            "Independent-spirited AI music that defies mainstream conventions.",                  "#7b2d8b", "🎵", 0));
        save.accept(genre("Alternative",      "Genre-blending AI music pushing boundaries of conventional rock and pop.",           "#457b9d", "🎸", 0));
        save.accept(genre("Indie Pop",        "Catchy, hook-driven AI pop with indie sensibilities and experimental production.",   "#ff6b35", "🌸", 1));
        save.accept(genre("Grunge",           "Distorted, raw AI guitar music channeling the angst of 90s Seattle grunge.",        "#495057", "🎸", 0));
        save.accept(genre("Punk",             "Fast, aggressive AI music rooted in the DIY ethos and rebellious spirit of punk.",  "#ff006e", "⚡", 0));

        // ── Metal ──────────────────────────────────────────────────────────────
        save.accept(genre("Metal",            "Heavy, distorted AI guitar music spanning classic metal to modern extremes.",        "#6c757d", "🤘", 0));
        save.accept(genre("Heavy Metal",      "AI-powered wall-of-sound metal with crushing riffs and powerful drumming.",         "#343a40", "🤘", 0));
        save.accept(genre("Death Metal",      "Extreme AI metal with blast beats, down-tuned guitars, and guttural vocals.",       "#212529", "💀", 0));
        save.accept(genre("Progressive Rock", "Complex AI compositions exploring extended song structures and odd time signatures.","#2b2d42", "🎵", 0));

        // ── Global & Specialty ─────────────────────────────────────────────────
        save.accept(genre("Reggae",           "Groove-heavy AI music rooted in Jamaican reggae tradition and social consciousness.", "#40916c", "🌿", 0));
        save.accept(genre("Latin",            "AI-generated Latin rhythms spanning salsa, bachata, reggaeton, and bossa nova.",    "#ff6b35", "💃", 0));
        save.accept(genre("Afrobeats",        "High-energy AI African-inspired rhythms blending highlife, fuji, and jùjú.",        "#2d6a4f", "🥁", 0));
        save.accept(genre("K-Pop",            "Polished, energetic AI Korean pop with synchronized production and catchy hooks.",  "#ff85a1", "🌸", 0));
        save.accept(genre("Gospel",           "Uplifting AI faith-based music rooted in the tradition of gospel and spiritual.",   "#ffd60a", "✝️", 0));
        save.accept(genre("Funk",             "Groove-first AI music built on syncopated bass lines and tight rhythm sections.",   "#fb8500", "🕺", 0));
        save.accept(genre("Disco",            "AI dance music channeling the glittering, four-on-the-floor spirit of the 70s.",   "#ffb703", "🕺", 0));
        save.accept(genre("World Music",      "AI explorations of global musical traditions, rhythms, and instrumentation.",       "#e9c46a", "🌍", 0));
        save.accept(genre("Bluegrass",        "Acoustic AI string music rooted in Appalachian bluegrass and old-time traditions.", "#8a9b68", "🪕", 0));

        // ── Atmospheric & Niche ────────────────────────────────────────────────
        save.accept(genre("Space Ambient",    "Expansive, cosmic AI soundscapes inspired by the universe and deep space.",         "#4d79ff", "🚀", 1));
        save.accept(genre("New Age",          "Gentle, spiritually-inspired AI music designed for meditation and inner peace.",    "#52b788", "🧘", 0));
        save.accept(genre("Experimental",     "Boundary-pushing AI compositions that defy categorisation and challenge convention.","#6c757d", "🔬", 0));

        log.info("Genre seeding complete.");
    }

    /** Convenience factory for Genre entities. */
    private Genre genre(String name, String description, String colorHex, String iconEmoji, int trackCount) {
        Genre g = new Genre();
        g.setName(name);
        g.setDescription(description);
        g.setColorHex(colorHex);
        g.setIconEmoji(iconEmoji);
        g.setTrackCount(trackCount);
        return g;
    }

    // ───────────────────────────────────────────────────────────────────────────
    // NEW RELEASES
    // ───────────────────────────────────────────────────────────────────────────

    private void seedNewReleases() {
        if (newReleaseRepository.count() > 0) return;
        log.info("Seeding new releases...");

        var tracks = trackRepository.findAll();
        if (tracks.isEmpty()) return;

        java.time.LocalDate today = java.time.LocalDate.now();
        String[] spotlights = {
            "A stunning debut that redefines what AI can do with electronic textures.",
            "Haunting and beautiful — AmbientCore proves silence can be engineered.",
            "Neural_Jazz's most ambitious release yet: pure algorithmic improvisation.",
            "DawnAI captures the feeling of sunrise in a single 3-minute lo-fi track.",
            "CosmicAI takes us to the edge of the universe with this hypnotic hymn."
        };

        int max = Math.min(tracks.size(), 5);
        for (int i = 0; i < max; i++) {
            NewRelease nr = new NewRelease();
            nr.setTrack(tracks.get(i));
            nr.setReleaseDate(today.minusDays(i * 3L));
            nr.setSpotlightText(spotlights[i]);
            nr.setFeaturedStatus(i < 2);
            newReleaseRepository.save(nr);
        }
    }
}
