package com.mikstermedia.repository;

import com.mikstermedia.model.PlatformSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link PlatformSetting} key-value metadata.
 *
 * <p>The PK is a String ({@code setting_key}), so {@code findById(key)}
 * is the primary lookup mechanism.
 */
@Repository
public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, String> {
}
