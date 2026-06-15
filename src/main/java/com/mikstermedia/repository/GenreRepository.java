package com.mikstermedia.repository;
import com.mikstermedia.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByNameIgnoreCase(String name);
    java.util.List<Genre> findByFeaturedStatusTrueOrderByDisplayOrderAsc();
}
