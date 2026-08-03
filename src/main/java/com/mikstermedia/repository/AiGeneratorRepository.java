package com.mikstermedia.repository;

import com.mikstermedia.model.AiGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiGeneratorRepository extends JpaRepository<AiGenerator, Long> {
    List<AiGenerator> findAllByOrderByDisplayOrderAsc();
    Optional<AiGenerator> findBySlug(String slug);
}
