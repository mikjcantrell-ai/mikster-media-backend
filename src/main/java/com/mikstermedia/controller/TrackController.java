package com.mikstermedia.controller;

import com.mikstermedia.dto.TrackDTO;
import com.mikstermedia.dto.TrackOrderDTO;
import com.mikstermedia.model.Track;
import com.mikstermedia.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing CRUD and query endpoints for {@link Track} resources.
 *
 * <p>Base path: {@code /api/tracks}
 *
 * <p>CORS is handled globally in {@link com.mikstermedia.config.CorsConfig} —
 * no per-controller {@code @CrossOrigin} annotations are needed.
 *
 * <p>Integration notes for Angular:
 * <ul>
 *   <li>HomeComponent calls {@code GET /api/tracks/featured} for the hero carousel.</li>
 *   <li>The search bar posts to {@code GET /api/tracks/search?keyword=...}.</li>
 *   <li>The streaming embed component calls {@code GET /api/tracks/{id}} to
 *       resolve the mediaUrl and platformSource before rendering the player.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    /** GET /api/tracks — returns all curated tracks */
    @GetMapping
    public ResponseEntity<List<Track>> getAllTracks() {
        return ResponseEntity.ok(trackService.getAllTracks());
    }

    /** GET /api/tracks/{id} — returns a single track by primary key */
    @GetMapping("/{id}")
    public ResponseEntity<Track> getTrackById(@PathVariable Long id) {
        return ResponseEntity.ok(trackService.getTrackById(id));
    }

    /** GET /api/tracks/featured — homepage hero carousel data source */
    @GetMapping("/featured")
    public ResponseEntity<List<Track>> getFeaturedTracks() {
        return ResponseEntity.ok(trackService.getFeaturedTracks());
    }

    /** GET /api/tracks/platform/{source} — filter by Spotify, YouTube, Apple */
    @GetMapping("/platform/{source}")
    public ResponseEntity<List<Track>> getByPlatform(@PathVariable String source) {
        return ResponseEntity.ok(trackService.getTracksByPlatform(source));
    }

    /** GET /api/tracks/search?keyword=... — title keyword search */
    @GetMapping("/search")
    public ResponseEntity<List<Track>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(trackService.searchByTitle(keyword));
    }

    /** GET /api/tracks/creator/{name} — fetch all tracks by a creator */
    @GetMapping("/creator/{name}")
    public ResponseEntity<List<Track>> getByCreator(@PathVariable String name) {
        return ResponseEntity.ok(trackService.getTracksByCreator(name));
    }

    /**
     * POST /api/tracks — create a new curated track.
     * Angular Admin panel sends a validated TrackDTO JSON payload.
     * Returns 201 Created with the persisted entity.
     */
    @PostMapping
    public ResponseEntity<Track> createTrack(@Valid @RequestBody TrackDTO dto) {
        Track created = trackService.createTrack(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/tracks/{id} — full update of an existing track */
    @PutMapping("/{id}")
    public ResponseEntity<Track> updateTrack(
            @PathVariable Long id,
            @Valid @RequestBody TrackDTO dto) {
        return ResponseEntity.ok(trackService.updateTrack(id, dto));
    }

    /**
     * POST /api/tracks/{id}/extend?days=N — admin extends a track's featured period.
     *
     * <p>If the track is currently featured and hasn't expired yet, days are added
     * on top of the existing expiry date. If it has already expired (or featuredUntil
     * was never set), the extension starts from today.
     *
     * @param id   the track to extend
     * @param days how many additional days (1–365, default 7)
     * @return the updated Track with the new featuredUntil date
     */
    @PostMapping("/{id}/extend")
    public ResponseEntity<Track> extendFeatured(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") int days) {
        try {
            return ResponseEntity.ok(trackService.extendFeatured(id, days));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** DELETE /api/tracks/{id} — removes a track; returns 204 No Content */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long id) {
        trackService.deleteTrack(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/tracks/reorder — admin bulk-updates displayOrder for featured tracks.
     *
     * <p>The Angular home page sends the complete ordered list after a drag-and-drop
     * operation. Spring Security ensures only authenticated admins can call this.
     *
     * @param orders list of {id, displayOrder} pairs
     */
    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderFeaturedTracks(@RequestBody List<TrackOrderDTO> orders) {
        trackService.reorderFeaturedTracks(orders);
        return ResponseEntity.noContent().build();
    }
}
