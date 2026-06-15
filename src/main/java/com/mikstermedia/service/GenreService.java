package com.mikstermedia.service;

import com.mikstermedia.model.Genre;
import com.mikstermedia.repository.GenreRepository;
import com.mikstermedia.repository.PlatformSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class GenreService {

    private static final int DEFAULT_FEATURED_DAYS = 30;

    private final GenreRepository repo;
    private final PlatformSettingRepository settingRepo;

    public GenreService(GenreRepository repo, PlatformSettingRepository settingRepo) {
        this.repo = repo;
        this.settingRepo = settingRepo;
    }

    private int featuredGenreDays() {
        return settingRepo.findById("GENRE_FEATURED_DAYS")
            .map(s -> {
                try { return Integer.parseInt(s.getSettingValue()); }
                catch (NumberFormatException e) { return DEFAULT_FEATURED_DAYS; }
            })
            .orElse(DEFAULT_FEATURED_DAYS);
    }

    private int featuredGenreLimit() {
        return settingRepo.findById("GENRE_FEATURED_LIMIT")
            .map(s -> {
                try { return Integer.parseInt(s.getSettingValue()); }
                catch (NumberFormatException e) { return 5; }
            })
            .orElse(5);
    }

    @Transactional
    public List<Genre> getFeatured() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(featuredGenreDays());
        List<Genre> all = repo.findByFeaturedStatusTrueOrderByDisplayOrderAsc();

        all.stream()
            .filter(g -> g.getFeaturedSince() != null && g.getFeaturedSince().isBefore(cutoff))
            .forEach(g -> {
                g.setFeaturedStatus(false);
                g.setFeaturedSince(null);
                repo.save(g);
            });

        return all.stream()
            .filter(Genre::isFeaturedStatus)
            .limit(featuredGenreLimit())
            .toList();
    }

    @Transactional
    public Genre featureNow(Long id) {
        Genre g = repo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Genre not found: " + id));
        g.setFeaturedStatus(true);
        if (g.getFeaturedSince() == null) {
            g.setFeaturedSince(LocalDateTime.now());
        }
        return repo.save(g);
    }

    @Transactional
    public Genre expireNow(Long id) {
        Genre g = repo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Genre not found: " + id));
        g.setFeaturedStatus(false);
        g.setFeaturedSince(null);
        return repo.save(g);
    }

    @Transactional
    public void reorder(List<Map<String, Integer>> orderItems) {
        for (Map<String, Integer> item : orderItems) {
            Long id = item.get("id").longValue();
            int order = item.get("displayOrder");
            repo.findById(id).ifPresent(g -> {
                g.setDisplayOrder(order);
                repo.save(g);
            });
        }
    }
}
