package com.mikstermedia.controller;

import com.mikstermedia.model.AiGenerator;
import com.mikstermedia.service.AiGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-generators")
@RequiredArgsConstructor
public class AiGeneratorController {

    private final AiGeneratorService aiGeneratorService;

    @GetMapping
    public ResponseEntity<List<AiGenerator>> getAllGenerators() {
        return ResponseEntity.ok(aiGeneratorService.getAllGenerators());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiGenerator> getGeneratorById(@PathVariable Long id) {
        return ResponseEntity.ok(aiGeneratorService.getGeneratorById(id));
    }

    @PostMapping
    public ResponseEntity<AiGenerator> createGenerator(@RequestBody AiGenerator generator) {
        return ResponseEntity.ok(aiGeneratorService.saveGenerator(generator));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AiGenerator> updateGenerator(@PathVariable Long id, @RequestBody AiGenerator generator) {
        generator.setId(id);
        return ResponseEntity.ok(aiGeneratorService.saveGenerator(generator));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenerator(@PathVariable Long id) {
        aiGeneratorService.deleteGenerator(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAllUrls() {
        aiGeneratorService.verifyAllGeneratorUrls();
        return ResponseEntity.ok(Map.of("message", "URL verification completed successfully"));
    }
}
