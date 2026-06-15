package com.mikstermedia.controller;

import com.mikstermedia.model.Genre;
import com.mikstermedia.repository.GenreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreRepository repo;
    private final com.mikstermedia.service.GenreService genreService;

    public GenreController(GenreRepository repo, com.mikstermedia.service.GenreService genreService) { 
        this.repo = repo; 
        this.genreService = genreService;
    }

    @GetMapping
    public List<Genre> getAll() { return repo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name")); }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/featured")
    public List<Genre> getFeatured() {
        return genreService.getFeatured();
    }

    @PostMapping("/{id}/feature")
    public ResponseEntity<Genre> featureGenre(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(genreService.featureNow(id));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/expire")
    public ResponseEntity<Genre> expireGenre(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(genreService.expireNow(id));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorderFeatured(@RequestBody List<java.util.Map<String, Integer>> updates) {
        genreService.reorder(updates);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public Genre create(@RequestBody Genre genre) { return repo.save(genre); }

    @PutMapping("/{id}")
    public ResponseEntity<Genre> update(@PathVariable Long id, @RequestBody Genre body) {
        return repo.findById(id).map(g -> {
            g.setName(body.getName());
            g.setDescription(body.getDescription());
            g.setColorHex(body.getColorHex());
            g.setIconEmoji(body.getIconEmoji());
            g.setTrackCount(body.getTrackCount());
            return ResponseEntity.ok(repo.save(g));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
