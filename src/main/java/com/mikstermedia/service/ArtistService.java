package com.mikstermedia.service;

import com.mikstermedia.model.Artist;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.repository.PlatformSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Business logic for {@link Artist} entities.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Retrieve featured artists, filtering out those whose window has expired.</li>
 *   <li>The featured duration is driven by the {@code featured_artist_days} platform setting
 *       (editable by admins in the Admin Dashboard). Defaults to 14 if not set.</li>
 *   <li>Expire a single artist's featured status on demand.</li>
 *   <li>Bulk-reorder featured artists by updating their {@code displayOrder}.</li>
 *   <li>Auto-set {@code featuredSince} when {@code featuredStatus} is toggled on.</li>
 * </ul>
 */
@Service
public class ArtistService {

    private static final int DEFAULT_FEATURED_DAYS = 14;

    private final ArtistRepository repo;
    private final PlatformSettingRepository settingRepo;

    public ArtistService(ArtistRepository repo, PlatformSettingRepository settingRepo) {
        this.repo = repo;
        this.settingRepo = settingRepo;
    }

    /** Reads the configured featured duration for artists from platform settings. */
    private int featuredArtistDays() {
        return settingRepo.findById("featured_artist_days")
            .map(s -> {
                try { return Integer.parseInt(s.getSettingValue()); }
                catch (NumberFormatException e) { return DEFAULT_FEATURED_DAYS; }
            })
            .orElse(DEFAULT_FEATURED_DAYS);
    }

    /** Returns all artists. */
    public List<Artist> getAll() {
        return repo.findAll();
    }

    /**
     * Returns currently featured artists sorted by {@code displayOrder}.
     * Artists whose {@code featuredSince} is older than {@link #FEATURED_DAYS} days
     * are automatically expired (status cleared) and excluded from the result.
     */
    @Transactional
    public List<Artist> getFeatured() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(featuredArtistDays());
        List<Artist> all = repo.findByFeaturedStatusTrue();

        // Auto-expire any that have passed the 14-day window
        all.stream()
            .filter(a -> a.getFeaturedSince() != null && a.getFeaturedSince().isBefore(cutoff))
            .forEach(a -> {
                a.setFeaturedStatus(false);
                a.setFeaturedSince(null);
                repo.save(a);
            });

        // Return only still-featured, sorted by displayOrder (null-safe)
        return all.stream()
            .filter(Artist::isFeaturedStatus)
            .sorted(Comparator.comparingInt(a -> a.getDisplayOrder() == null ? 0 : a.getDisplayOrder()))
            .toList();
    }

    /**
     * Immediately expires (un-features) a single artist.
     *
     * @throws NoSuchElementException if the artist is not found
     */
    @Transactional
    public Artist expireNow(Long id) {
        Artist a = repo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Artist not found: " + id));
        a.setFeaturedStatus(false);
        a.setFeaturedSince(null);
        return repo.save(a);
    }

    /**
     * Bulk-updates {@code displayOrder} for featured artists.
     *
     * @param orderItems list of maps containing {@code "id"} and {@code "displayOrder"}
     */
    @Transactional
    public void reorder(List<Map<String, Integer>> orderItems) {
        for (Map<String, Integer> item : orderItems) {
            Long id = item.get("id").longValue();
            int order = item.get("displayOrder");
            repo.findById(id).ifPresent(a -> {
                a.setDisplayOrder(order);
                repo.save(a);
            });
        }
    }

    /**
     * Saves an artist. If {@code featuredStatus} is being set to {@code true}
     * and {@code featuredSince} is not yet set, records the current timestamp.
     */
    @Transactional
    public Artist save(Artist artist) {
        if (artist.isFeaturedStatus() && artist.getFeaturedSince() == null) {
            artist.setFeaturedSince(LocalDateTime.now());
        } else if (!artist.isFeaturedStatus()) {
            // Cleared by admin — wipe the timestamp
            artist.setFeaturedSince(null);
        }
        return repo.save(artist);
    }

    /**
     * Updates an existing artist by ID, preserving expiry logic.
     *
     * @throws NoSuchElementException if the artist is not found
     */
    @Transactional
    public Artist update(Long id, Artist body) {
        Artist a = repo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Artist not found: " + id));

        a.setName(body.getName());
        a.setBio(body.getBio());
        a.setCountry(body.getCountry());
        a.setAiToolsUsed(body.getAiToolsUsed());
        a.setPrimaryGenre(body.getPrimaryGenre());
        a.setImageUrl(body.getImageUrl());
        a.setProfileUrl(body.getProfileUrl());
        a.setWebsiteUrl(body.getWebsiteUrl());
        a.setMonthlyListeners(body.getMonthlyListeners());
        a.setDisplayOrder(body.getDisplayOrder());

        // Handle featuredStatus toggle with timestamp management
        boolean wasFeature = a.isFeaturedStatus();
        boolean nowFeature = body.isFeaturedStatus();
        a.setFeaturedStatus(nowFeature);
        if (!wasFeature && nowFeature) {
            // Newly featured — stamp the time
            a.setFeaturedSince(LocalDateTime.now());
        } else if (!nowFeature) {
            // Un-featured — clear timestamp
            a.setFeaturedSince(null);
        }
        // If already featured, preserve existing featuredSince

        return repo.save(a);
    }
}
