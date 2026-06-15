package com.mikstermedia.controller;

import com.mikstermedia.model.PlatformSetting;
import com.mikstermedia.repository.PlatformSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing platform-level site settings stored as key-value pairs.
 *
 * <p>Base path: {@code /api/settings}
 *
 * <p>Angular uses {@code GET /api/settings/site_title} to populate the browser
 * title and navbar brand name dynamically without a hard-coded frontend constant.
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class PlatformSettingController {

    private final PlatformSettingRepository settingRepository;

    /** GET /api/settings — returns all settings as a flat list */
    @GetMapping
    public ResponseEntity<List<PlatformSetting>> getAllSettings() {
        return ResponseEntity.ok(settingRepository.findAll());
    }

    /** GET /api/settings/{key} — returns a single setting by its string key */
    @GetMapping("/{key}")
    public ResponseEntity<PlatformSetting> getSetting(@PathVariable String key) {
        return settingRepository.findById(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/settings/{key} — upserts a setting value.
     * Since the PK is the key itself, saving an existing key updates its value.
     */
    @PutMapping("/{key}")
    public ResponseEntity<PlatformSetting> upsertSetting(
            @PathVariable String key,
            @RequestBody String value) {
        PlatformSetting setting = new PlatformSetting(key, value.trim().replace("\"", ""));
        return ResponseEntity.status(HttpStatus.OK).body(settingRepository.save(setting));
    }
}
