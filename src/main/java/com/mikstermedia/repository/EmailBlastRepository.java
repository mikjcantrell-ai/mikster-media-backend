package com.mikstermedia.repository;

import com.mikstermedia.model.EmailBlast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailBlastRepository extends JpaRepository<EmailBlast, Long> {
    List<EmailBlast> findAllByOrderBySentAtDesc();
    List<EmailBlast> findByTypeIgnoreCaseOrderBySentAtDesc(String type);
}
