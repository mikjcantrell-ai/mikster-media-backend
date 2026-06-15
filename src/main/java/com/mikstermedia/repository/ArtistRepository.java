package com.mikstermedia.repository;
import com.mikstermedia.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    List<Artist> findByFeaturedStatusTrue();
    List<Artist> findByPrimaryGenreIgnoreCase(String genre);
    List<Artist> findByNameContainingIgnoreCase(String name);
    java.util.Optional<Artist> findByNameIgnoreCase(String name);
}
