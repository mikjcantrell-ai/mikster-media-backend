package com.mikstermedia.repository;

import com.mikstermedia.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);
    List<AppUser> findByRole(String role);
    List<AppUser> findByActiveTrue();
    boolean existsByUsernameIgnoreCase(String username);
}
