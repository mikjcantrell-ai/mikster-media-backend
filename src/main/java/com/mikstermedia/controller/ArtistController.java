package com.mikstermedia.controller;

import com.mikstermedia.model.Artist;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.repository.TrackRepository;
import com.mikstermedia.repository.WeeklyChartRepository;
import com.mikstermedia.service.ArtistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for {@link Artist} CRUD and featured-artist management.
 *
 * <p>Base path: {@code /api/artists}
 *
 * <ul>
 *   <li>GET  /api/artists             — public: all artists</li>
 *   <li>GET  /api/artists/featured    — public: currently-featured (auto-expires stale ones)</li>
 *   <li>GET  /api/artists/{id}        — public: single artist by id</li>
 *   <li>GET  /api/artists/name/{name} — public: single artist by name</li>
 *   <li>GET  /api/artists/search      — public: name search</li>
 *   <li>GET  /api/artists/genre/{g}   — public: filter by genre</li>
 *   <li>GET  /api/artists/{id}/track-count — admin: how many tracks will be deleted with this artist</li>
 *   <li>POST /api/artists             — admin: create</li>
 *   <li>PUT  /api/artists/{id}        — admin: full update (handles featuredSince stamp)</li>
 *   <li>PATCH /api/artists/{id}/expire — admin: immediately un-feature</li>
 *   <li>PATCH /api/artists/reorder     — admin: bulk-update displayOrder</li>
 *   <li>DELETE /api/artists/{id}       — admin: delete artist + all their tracks</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistRepository repo;
    private final ArtistService artistService;
    private final TrackRepository trackRepo;
    private final WeeklyChartRepository chartRepo;

    public ArtistController(ArtistRepository repo, ArtistService artistService,
                            TrackRepository trackRepo, WeeklyChartRepository chartRepo) {
        this.repo = repo;
        this.artistService = artistService;
        this.trackRepo = trackRepo;
        this.chartRepo = chartRepo;
    }

    @GetMapping
    public List<Artist> getAll() { return artistService.getAll(); }

    /**
     * GET /api/artists/featured
     * Returns featured artists sorted by displayOrder; auto-expires those older than 14 days.
     */
    @GetMapping("/featured")
    public List<Artist> getFeatured() { return artistService.getFeatured(); }

    @GetMapping("/{id}")
    public ResponseEntity<Artist> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Artist> getByName(@PathVariable String name) {
        return repo.findByNameIgnoreCase(name).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Artist> search(@RequestParam String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/genre/{genre}")
    public List<Artist> byGenre(@PathVariable String genre) {
        return repo.findByPrimaryGenreIgnoreCase(genre);
    }

    /** POST /api/artists — create a new artist. Sets featuredSince if featuredStatus=true. */
    @PostMapping
    public Artist create(@RequestBody Artist artist) { return artistService.save(artist); }

    /** PUT /api/artists/{id} — full update with featuredSince timestamp management. */
    @PutMapping("/{id}")
    public ResponseEntity<Artist> update(@PathVariable Long id, @RequestBody Artist body) {
        try {
            return ResponseEntity.ok(artistService.update(id, body));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/artists/{id}/expire — immediately un-features an artist.
     * Admin only (secured by SecurityConfig PATCH rule).
     */
    @PatchMapping("/{id}/expire")
    public ResponseEntity<Artist> expireNow(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(artistService.expireNow(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/artists/reorder — bulk-updates displayOrder for the featured carousel.
     * Body: [{id: 1, displayOrder: 0}, {id: 2, displayOrder: 1}, ...]
     * Admin only (secured by SecurityConfig PATCH rule).
     */
    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody List<Map<String, Integer>> orderItems) {
        artistService.reorder(orderItems);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/artists/{id}/track-count
     * Returns artistName + trackCount so the admin UI can show an informed
     * confirmation dialog before performing the cascade delete.
     */
    @GetMapping("/{id}/track-count")
    public ResponseEntity<Map<String, Object>> trackCount(@PathVariable Long id) {
        return repo.findById(id).map(artist -> {
            long count = trackRepo.findByCreatorContainingIgnoreCase(artist.getName()).size();
            return ResponseEntity.ok(Map.<String, Object>of(
                "artistName", artist.getName(),
                "trackCount", count
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/artists/{id}
     * Cascade-deletes all tracks (and weekly-chart rows) whose creator field
     * contains the artist name, then removes the artist record itself.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return repo.findById(id).map(artist -> {
            // 1. Find all tracks belonging to this artist
            var tracks = trackRepo.findByCreatorContainingIgnoreCase(artist.getName());

            // 2. Delete weekly-chart entries for those tracks first (FK constraint)
            tracks.forEach(t -> chartRepo.deleteByTrackId(t.getId()));

            // 3. Delete the tracks
            trackRepo.deleteAll(tracks);

            // 4. Delete the artist
            repo.delete(artist);

            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
