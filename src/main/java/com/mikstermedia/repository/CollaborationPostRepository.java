package com.mikstermedia.repository;

import com.mikstermedia.model.CollaborationPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.time.LocalDateTime;

public interface CollaborationPostRepository extends JpaRepository<CollaborationPost, Long> {
    List<CollaborationPost> findByActiveTrueAndCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);
}
