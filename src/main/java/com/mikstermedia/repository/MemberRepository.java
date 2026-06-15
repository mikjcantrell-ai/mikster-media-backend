package com.mikstermedia.repository;

import com.mikstermedia.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<Member> findByEmailIgnoreCase(String email);
    Optional<Member> findByUsernameIgnoreCase(String username);
}
