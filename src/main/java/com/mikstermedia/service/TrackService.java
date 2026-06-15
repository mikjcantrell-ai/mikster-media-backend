package com.mikstermedia.service;

import com.mikstermedia.dto.TrackDTO;
import com.mikstermedia.dto.TrackOrderDTO;
import com.mikstermedia.model.Track;
import com.mikstermedia.repository.NewReleaseRepository;
import com.mikstermedia.repository.TrackRepository;
import com.mikstermedia.repository.WeeklyChartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

/**
 * Business-logic layer for {@link Track} management.
 *
 * <p>All database interactions are delegated to {@link TrackRepository}.
 * Controller methods map validated DTOs to entities here before persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrackService {

    private final TrackRepository       trackRepository;
    private final NewReleaseRepository   newReleaseRepository;
    private final WeeklyChartRepository  weeklyChartRepository;
    private final WeeklyChartService     weeklyChartService;

    // ─────────────────────────────────────────────────────────────────────────
    // READ operations
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns every track in the database — used by browse / admin views. */
    @Transactional(readOnly = true)
    public List<Track> getAllTracks() {
        log.debug("Fetching all tracks");
        return trackRepository.findAll();
    }

    /** Returns a single track by ID, or throws if not found. */
    @Transactional(readOnly = true)
    public Track getTrackById(Long id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track not found with id: " + id));
    }

    /** Returns featured tracks sorted by displayOrder, filtering out expired ones. */
    @Transactional(readOnly = true)
    public List<Track> getFeaturedTracks() {
        LocalDate today = LocalDate.now();
        return trackRepository.findByFeaturedStatusTrueOrderByDisplayOrderAsc()
                .stream()
                // Keep tracks with no expiry date set OR whose expiry is today/future
                .filter(t -> t.getFeaturedUntil() == null || !t.getFeaturedUntil().isBefore(today))
                .toList();
    }

    /** Filters tracks by streaming platform (Spotify / YouTube / Apple). */
    @Transactional(readOnly = true)
    public List<Track> getTracksByPlatform(String platform) {
        return trackRepository.findByPlatformSource(platform);
    }

    /** Case-insensitive title search for the search bar. */
    @Transactional(readOnly = true)
    public List<Track> searchByTitle(String keyword) {
        return trackRepository.findByTitleContainingIgnoreCase(keyword);
    }

    /** Exact match by creator name for portfolio pages. */
    @Transactional(readOnly = true)
    public List<Track> getTracksByCreator(String creator) {
        return trackRepository.findByCreatorContainingIgnoreCase(creator);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WRITE operations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new track from the validated DTO payload.
     *
     * @param dto validated TrackDTO received from the controller layer
     * @return the persisted Track with its generated ID
     */
    public Track createTrack(TrackDTO dto) {
        log.info("Creating track: {}", dto.getTitle());
        Track track = new Track();
        mapDtoToEntity(dto, track);
        Track saved = trackRepository.save(track);
        weeklyChartService.recalculateRankings();
        return saved;
    }

    /**
     * Updates an existing track.  Only fields present in the DTO are updated;
     * the ID remains unchanged.
     */
    public Track updateTrack(Long id, TrackDTO dto) {
        Track existing = getTrackById(id);
        mapDtoToEntity(dto, existing);
        log.info("Updating track id={}", id);
        Track saved = trackRepository.save(existing);
        weeklyChartService.recalculateRankings();
        return saved;
    }

    /**
     * Admin: extends a track's featured period by {@code days} additional days.
     * If the track's featuredUntil is already in the future, days are added on top.
     * If it has expired (or was never set), the extension starts from today.
     * Always ensures featuredStatus = true.
     *
     * @param id   track ID
     * @param days number of days to extend (1–365)
     * @return the updated Track
     */
    public Track extendFeatured(Long id, int days) {
        if (days < 1 || days > 365) throw new IllegalArgumentException("days must be 1–365");
        Track track = getTrackById(id);
        LocalDate today = LocalDate.now();
        LocalDate base = (track.getFeaturedUntil() != null && track.getFeaturedUntil().isAfter(today))
                         ? track.getFeaturedUntil()   // extend from current expiry
                         : today;                      // expired or not set — start from today
        track.setFeaturedUntil(base.plusDays(days));
        track.setFeaturedStatus(true);
        log.info("Extended featured for track id={} '{}' by {} days → until {}",
                 id, track.getTitle(), days, track.getFeaturedUntil());
        return trackRepository.save(track);
    }

    /**
     * Admin-only: bulk-updates displayOrder for a list of featured tracks.
     * The Angular frontend sends the full ordered list after a drag-and-drop.
     *
     * @param orders list of {id, displayOrder} pairs
     */
    public void reorderFeaturedTracks(List<TrackOrderDTO> orders) {
        log.info("Reordering {} featured tracks", orders.size());
        orders.forEach(o -> {
            trackRepository.findById(o.getId()).ifPresent(track -> {
                track.setDisplayOrder(o.getDisplayOrder());
                trackRepository.save(track);
            });
        });
    }

    /**
     * Hard-deletes a track and all dependent records.
     * Removes NewRelease and WeeklyChart rows first to avoid FK constraint violations.
     */
    public void deleteTrack(Long id) {
        log.info("Deleting track id={} — removing child records first", id);
        newReleaseRepository.deleteByTrackId(id);
        weeklyChartRepository.deleteByTrackId(id);
        trackRepository.deleteById(id);
        weeklyChartService.recalculateRankings();
        log.info("Track id={} deleted successfully", id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Copies fields from a DTO into a Track entity.
     * Centralised here so both create and update share the same mapping logic.
     */
    private void mapDtoToEntity(TrackDTO dto, Track track) {
        track.setTitle(dto.getTitle());
        track.setCreator(dto.getCreator());
        track.setMediaUrl(dto.getMediaUrl());
        track.setPlatformSource(dto.getPlatformSource());
        track.setAiToolsUsed(dto.getAiToolsUsed());
        track.setGenre(dto.getGenre());
        track.setFeaturedStatus(dto.isFeaturedStatus());
        track.setEmbedUrl(dto.getEmbedUrl());
        track.setVideoUrl(dto.getVideoUrl());
        track.setAiSourceUrl(dto.getAiSourceUrl());
        track.setImageUrl(dto.getImageUrl());
        track.setPromptRecipe(dto.getPromptRecipe());
        if (dto.getSpotifyPopularity() != null) {
            track.setSpotifyPopularity(dto.getSpotifyPopularity());
        }
        if (dto.getLastFmScrobbles() != null) {
            track.setLastFmScrobbles(dto.getLastFmScrobbles());
        }
        if (dto.getYoutubeViews() != null) {
            track.setYoutubeViews(dto.getYoutubeViews().intValue());
        }
        if (dto.getSunoPlays() != null) {
            track.setSunoPlays(dto.getSunoPlays());
        }
        if (dto.getUdioPlays() != null) {
            track.setUdioPlays(dto.getUdioPlays());
        }
        if (dto.getTiktokViews() != null) {
            track.setTiktokViews(dto.getTiktokViews());
        }
        if (dto.getChartmetricScore() != null) {
            track.setChartmetricScore(dto.getChartmetricScore());
        }
        if (dto.getReleaseDate() != null) {
            track.setReleaseDate(dto.getReleaseDate());
        }
    }
}
