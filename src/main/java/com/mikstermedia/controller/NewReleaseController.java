package com.mikstermedia.controller;
 
import com.mikstermedia.model.NewRelease;
import com.mikstermedia.model.Track;
import com.mikstermedia.repository.NewReleaseRepository;
import com.mikstermedia.repository.TrackRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;
 
@RestController
@RequestMapping("/api/new-releases")
public class NewReleaseController {
 
    private final NewReleaseRepository repo;
    private final TrackRepository trackRepo;
 
    public NewReleaseController(NewReleaseRepository repo, TrackRepository trackRepo) {
        this.repo = repo;
        this.trackRepo = trackRepo;
    }
 
    private List<NewRelease> getAutoPopulatedReleases() {
        List<NewRelease> explicitReleases = repo.findAll();
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        
        Set<Long> explicitTrackIds = explicitReleases.stream()
                .map(nr -> nr.getTrack().getId())
                .collect(Collectors.toSet());
        
        List<NewRelease> combined = new ArrayList<>(explicitReleases);
        
        for (Track t : trackRepo.findAll()) {
            if (t.getReleaseDate() != null && t.getReleaseDate().length() >= 10 && !explicitTrackIds.contains(t.getId())) {
                try {
                    LocalDate releaseDate = LocalDate.parse(t.getReleaseDate().substring(0, 10));
                    if (!releaseDate.isBefore(thirtyDaysAgo)) {
                        NewRelease transientRelease = new NewRelease();
                        transientRelease.setId(null);
                        transientRelease.setTrack(t);
                        transientRelease.setReleaseDate(releaseDate);
                        transientRelease.setSpotlightText("");
                        transientRelease.setFeaturedStatus(false);
                        transientRelease.setPlayCount(0);
                        combined.add(transientRelease);
                    }
                } catch (Exception ignored) {}
            }
        }
        
        combined.sort((a, b) -> b.getReleaseDate().compareTo(a.getReleaseDate()));
        return combined;
    }

    @GetMapping
    public List<NewRelease> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return getAutoPopulatedReleases().stream()
            .filter(nr -> {
                if (startDate != null && nr.getReleaseDate().isBefore(startDate)) return false;
                if (endDate != null && nr.getReleaseDate().isAfter(endDate)) return false;
                return true;
            }).collect(Collectors.toList());
    }

    @GetMapping("/featured")
    public List<NewRelease> getFeatured() {
        return getAutoPopulatedReleases().stream()
            .filter(NewRelease::isFeaturedStatus)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewRelease> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody NewRelease body) {
        if (body.getTrack() == null || body.getTrack().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "track.id is required"));
        }
        Track track = trackRepo.findById(body.getTrack().getId()).orElse(null);
        if (track == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Track not found with id: " + body.getTrack().getId()));
        }
        body.setTrack(track);
        return ResponseEntity.ok(repo.save(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody NewRelease body) {
        return repo.findById(id).map(r -> {
            r.setReleaseDate(body.getReleaseDate());
            r.setSpotlightText(body.getSpotlightText());
            r.setFeaturedStatus(body.isFeaturedStatus());
            if (body.getTrack() != null && body.getTrack().getId() != null) {
                trackRepo.findById(body.getTrack().getId()).ifPresent(r::setTrack);
            }
            return ResponseEntity.ok(repo.save(r));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
