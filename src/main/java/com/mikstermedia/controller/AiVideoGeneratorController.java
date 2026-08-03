package com.mikstermedia.controller;

import com.mikstermedia.model.AiVideoGenerator;
import com.mikstermedia.service.AiVideoGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-video-generators")
@RequiredArgsConstructor
public class AiVideoGeneratorController {

    private final AiVideoGeneratorService aiVideoGeneratorService;

    @GetMapping
    public ResponseEntity<List<AiVideoGenerator>> getAllGenerators() {
        return ResponseEntity.ok(aiVideoGeneratorService.getAllGenerators());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiVideoGenerator> getGeneratorById(@PathVariable Long id) {
        return ResponseEntity.ok(aiVideoGeneratorService.getGeneratorById(id));
    }

    @PostMapping
    public ResponseEntity<AiVideoGenerator> createGenerator(@RequestBody AiVideoGenerator generator) {
        return ResponseEntity.ok(aiVideoGeneratorService.saveGenerator(generator));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AiVideoGenerator> updateGenerator(@PathVariable Long id, @RequestBody AiVideoGenerator generator) {
        generator.setId(id);
        return ResponseEntity.ok(aiVideoGeneratorService.saveGenerator(generator));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenerator(@PathVariable Long id) {
        aiVideoGeneratorService.deleteGenerator(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAllUrls() {
        aiVideoGeneratorService.verifyAllGeneratorUrls();
        return ResponseEntity.ok(Map.of("message", "Video URL verification completed successfully"));
    }
}
