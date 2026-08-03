package com.mikstermedia.repository;

import com.mikstermedia.model.AiVideoGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiVideoGeneratorRepository extends JpaRepository<AiVideoGenerator, Long> {
    Optional<AiVideoGenerator> findBySlug(String slug);
    List<AiVideoGenerator> findAllByOrderByDisplayOrderAsc();
}
