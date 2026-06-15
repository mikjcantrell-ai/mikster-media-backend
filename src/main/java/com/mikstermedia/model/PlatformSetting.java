package com.mikstermedia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * JPA entity storing arbitrary key-value platform configuration metadata.
 *
 * <p>Examples of stored settings:
 * <ul>
 *   <li>{@code site_title} → "AI Music Web"</li>
 *   <li>{@code featured_genre} → "Electronic"</li>
 *   <li>{@code chart_refresh_hour} → "0"</li>
 * </ul>
 *
 * <p>The primary key is {@code setting_key} (a String), so there is no
 * auto-increment ID on this table.
 */
@Entity
@Table(name = "platform_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSetting {

    /**
     * Unique string identifier for the setting.
     * Acts as the natural primary key — no surrogate ID.
     */
    @Id
    @Column(name = "setting_key", nullable = false)
    private String settingKey;

    /** Serialized setting value (always stored as a string). */
    @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
    private String settingValue;
}
